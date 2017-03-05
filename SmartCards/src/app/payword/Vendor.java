package app.payword;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.model.Commitment;
import app.payword.model.Product;
import app.payword.model.Receipt;
import app.payword.network.Protocol;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class Vendor extends Servent
{
	private Map<Integer, ServentIdentity> identityMap;
	private Map<Integer, Commitment> commitmentMap;

	private static List<Product> catalogue;

	public Vendor()
	{
		super(Logger.getLogger("Vendor"), 300, "127.0.0.3", 6799);
		this.identityMap   = new HashMap<>();
		this.commitmentMap = new HashMap<>();
		// this.startWatchdogProcess();
	}

	static
	{
		catalogue = new ArrayList<>();
		catalogue.add(new Product("apples" , 12.0 ));
		catalogue.add(new Product("kiwi"   ,  0.67));
		catalogue.add(new Product("avocado",  9.67));
	}
	
	@Override
	public void onReceiveIncomingConnection(Socket hostSocket)
	{
		/*
		 * Incoming requests will occur only from Users (clients).
		 */
		boolean closingSignal = false;

		boolean isCommitmentExpired     = false;
		boolean isCommitmentValueEnough = true;
		boolean isPaywordValid = true;
		boolean isPaywordEnough = false;
		
		boolean isCommitmentAuthentic   = false;
		boolean needMorePaywords = false;
		Integer identityNumber = null;
		
		String message   = "";
		String command   = "";
		String arguments = "";
		Receipt receipt = new Receipt();
		Commitment commitment = null;

		while(!closingSignal)
		{
			try
			{
				message = receive(hostSocket);
				if(message.contains(" "))
				{
					command   = message.substring(0, message.indexOf(" "));
					arguments = message.substring(message.indexOf(" ") + 1);
				}
				else
					command = message;

				switch (command)
				{
					case Protocol.Command.helloFromUser :
						{
							ServentIdentity userIdentity = ServentIdentity.decode(arguments);
							identityMap.put(userIdentity.getIdentityNumber(), userIdentity);
							identityNumber = userIdentity.getIdentityNumber();
							send(hostSocket, Protocol.Command.helloFromVendor + Protocol.Command.sep + getOwnIdentity().encode());
						}
						break;
					case Protocol.Command.productsCatalogueRequest :
						send(hostSocket, Protocol.Command.productsCatalogueOffer + Protocol.Command.sep + getCatalogue());
						break;
					case Protocol.Command.productReservationRequest :
						receipt.addProduct(catalogue.get(Integer.valueOf(arguments)));
						send(hostSocket, Protocol.Command.productReservationAccepted);
						// if we do not have enough quantity of it..
						// send(hostSocket, Protocol.Command.productReservationRejected);
						break;
					case Protocol.Command.receiptRequest :
						send(hostSocket, Protocol.Command.receiptOffer + Protocol.Command.sep + receipt.getTotalAmount());
						break;
					case Protocol.Command.receiptAcknowleged :
						{
							if(!commitmentMap.containsKey(identityNumber))
								send(hostSocket, Protocol.Command.commitmentRequest);
							else if (isCommitmentExpired)
								send(hostSocket, Protocol.Command.commitmentExpiredRequestNewOne);
							else if(!isCommitmentValueEnough)
								send(hostSocket, Protocol.Command.commitmentValueNotEnoughRequestAdditionalOne + Protocol.Command.sep + " ");
							else
								send(hostSocket, Protocol.Command.commitmentPaywordRequest); // !! Attention, next step must complete with success otherwise ABORTbreak;
						}
						break;
					case Protocol.Command.commitmentOffer :
						{		
							commitment = Commitment.decode(arguments.substring(0, arguments.lastIndexOf(" ")));
							String     signature  = arguments.substring(arguments.lastIndexOf(" ") + 1);
							isCommitmentAuthentic = Commitment.isSignatureAuthentic(commitment, signature, commitment.getUserCertificate().getUserEncodedPublicKey());
							// check the signature of the Certificate. This requires that the Vendor obtains the brokers
							// public key in a previous step
							if(isCommitmentAuthentic)
							{
								commitmentMap.put(identityNumber, commitment);
								send(hostSocket, Protocol.Command.commitmentPaywordRequest);
							}
							else
							{
								send(hostSocket, Protocol.Command.commitmentRejectedReceiptAborted + Protocol.Command.sep + "Commitment not accepted!");
								receipt.clear();
							}
						}
					break;
					case Protocol.Command.commitmentPaywordOffer :
						{
							String  paywordValue = arguments.substring(1, arguments.lastIndexOf(","));
							Integer paywordIndex = Integer.valueOf(arguments.substring(arguments.lastIndexOf(",") + 1, arguments.indexOf(")")));
							isPaywordValid = commitment.isPaywordValid(paywordValue, paywordIndex);
							if(isPaywordValid)
							{
								Double amountReceived = commitment.processPayword(paywordValue, paywordIndex);
			
								if(receipt.coversCost(amountReceived))
									send(hostSocket, Protocol.Command.commitmentPaywordAcceptedReceiptFinalized + Protocol.Command.sep + receipt);
								else if(receipt.exceedsCost(amountReceived))
									send(hostSocket, Protocol.Command.commitmentPaywordExceedsReceiptAborted);
								else // Aici ar trebui sa tratam cazurile cu rest..
									send(hostSocket, Protocol.Command.commitmentPaywordExceedsReceiptAborted);
							}
							else
							{
								send(hostSocket, Protocol.Command.commitmentPaywordRejectedReceiptAborted + Protocol.Command.sep + "Everything is aborted");
								receipt.clear();
							}
						}
						break;
					case Protocol.Command.goodbyeFromUser:
						send(hostSocket, Protocol.Command.goodbyeFromVendor);
						break;
					default:
						send(hostSocket, Protocol.Command.commandError);
						break;
				}
			} catch( Exception e)
			{
				logger.error(e);
				logger.info("No further messages will be exchanged!");
				return;
			}
		}
	}
	
	public String getCatalogue()
	{
		StringBuilder sb = new StringBuilder();
		for(int counter = 0; counter < catalogue.size(); counter++)
			sb.append(counter + "::" + catalogue.get(counter) + "::" + catalogue.get(counter).getPrice());
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Vendor broker = new Vendor();
		broker.start();
	}

}
