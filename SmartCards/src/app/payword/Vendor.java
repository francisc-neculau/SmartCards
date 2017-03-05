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
import app.payword.network.Protocol;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class Vendor extends Servent
{
	private Map<Integer, ServentIdentity> identityMap;
	private Map<Integer, Commitment> centCommitmentMap;
	private Map<Integer, Commitment> unitCommitmentMap;

	private static List<Product> productsList;

	public Vendor()
	{
		super(Logger.getLogger("Vendor"), 300, "127.0.0.3", 6799);
		this.identityMap   = new HashMap<>();
		this.centCommitmentMap = new HashMap<>();
		this.unitCommitmentMap = new HashMap<>();
	}

	static
	{
		productsList = new ArrayList<>();
		productsList.add(new Product("apples", 12));
		productsList.add(new Product("kiwi"  , 0.67));
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
		boolean everythingIsOk = true;
		
		boolean isCommitmentAvailable   = false;
		boolean isCommitmentAuthentic   = false;
		boolean isCertificateAtuthentic = false;
		boolean needMorePaywords = false;
		Integer identityNumber = null;
		
		String message   = "";
		String command   = "";
		String arguments = "";
		
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
						send(hostSocket, Protocol.Command.productsCatalogueOffer);
						break;
					case Protocol.Command.productReservationRequest :
						send(hostSocket, Protocol.Command.productReservationAccepted);
						// if we do not have enough quantity of it..
						// send(hostSocket, Protocol.Command.productReservationRejected);
						break;
					case Protocol.Command.receiptRequest :
						send(hostSocket, Protocol.Command.receiptOffer);
						break;
					case Protocol.Command.receiptAcknowleged :
						{
							if(!unitCommitmentMap.containsKey(identityNumber) || !centCommitmentMap.containsKey(identityNumber))
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
							Commitment commitment = Commitment.decode(arguments.substring(0, arguments.lastIndexOf(" ")));
							String     signature  = arguments.substring(arguments.lastIndexOf(" ") + 1);
							isCommitmentAuthentic = Commitment.isSignatureAuthentic(commitment, signature, commitment.getUserCertificate().getUserEncodedPublicKey());
							// check the signature of the Certificate. This requires that the Vendor obtains the brokers
							// public key in a previous step
							if(isCommitmentAuthentic)
							{
								if(commitment.isUnitType())
									unitCommitmentMap.put(identityNumber, commitment);
								else
									centCommitmentMap.put(identityNumber, commitment);
								send(hostSocket, Protocol.Command.commitmentPaywordRequest);
							}
							else
								send(hostSocket, Protocol.Command.commitmentRejectedReceiptAborted + Protocol.Command.sep + "Commitment not accepted!");
						}
					break;
					case Protocol.Command.commitmentPaywordOffer :
						{
							if(everythingIsOk)
								send(hostSocket, Protocol.Command.commitmentPaywordAcceptedReceiptFinalized + Protocol.Command.sep + "Products");
							else if(needMorePaywords)
								send(hostSocket, Protocol.Command.commitmentPaywordAcceptedMoreNeeded + Protocol.Command.sep + "Amount");
							else
								send(hostSocket, Protocol.Command.commitmentPaywordRejectedReceiptAborted + Protocol.Command.sep + "Everything is aborted");
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
		sb.append(productsList.get(0));
		sb.append(productsList.get(1));
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Vendor broker = new Vendor();
		broker.start();
	}

}
