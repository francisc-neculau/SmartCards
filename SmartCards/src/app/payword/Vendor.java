package app.payword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import app.payword.network.Protocol.Command;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;
import app.payword.util.PaywordConfiguration;

public class Vendor extends Servent
{
	private Map<Integer, ServentIdentity> identityMap;
	private Map<Integer, Commitment> commitmentMap;

	private static List<Product> catalogue;
	
	private ServentIdentity brokerIdentity;
	private Logger logger;
	
	public Vendor(ServentIdentity brokerIdentity)
	{
		super(PaywordConfiguration.VENDOR_LOGGER_NAME, PaywordConfiguration.VENDOR_IDENTITY_NUMBER, PaywordConfiguration.VENDOR_IP_ADDRESS, PaywordConfiguration.VENDOR_PORT_NUMBER);
		this.logger = Logger.getLogger(PaywordConfiguration.VENDOR_LOGGER_NAME);
		this.identityMap   = new HashMap<>();
		this.commitmentMap = new HashMap<>();
		
		this.brokerIdentity = brokerIdentity;
		this.startWatchdogProcess();
	}

	static
	{
		catalogue = new ArrayList<>();
		catalogue.add(new Product("apples" , 12.0 ));
		catalogue.add(new Product("kiwi"   ,  0.67));
		catalogue.add(new Product("avocado",  9.67));
	}

	public void redeemPaywords()
	{
		logger.info("Start sending...");
		Socket brokerSocket = connectToServant(brokerIdentity);
		send(brokerSocket, Command.helloFromVendor);
		
		String message = receive(brokerSocket); // receive the hello message from Broker
		send(brokerSocket, Command.paywordRedeemRequest);
		message	= receive(brokerSocket); // receive the accept redeem message from Broker
		
		for(Map.Entry<Integer, Commitment> entry : commitmentMap.entrySet())
		{
			send(brokerSocket, Command.paywordSendReceipt + Command.sep + entry.getValue().encode() + Command.sep 
						+ entry.getValue().getLastPaywordValue() + Command.sep + entry.getValue().getLastPaywordIndex());
		}
		
		send(brokerSocket,Command.paywordSendReceiptEndSignal);
		message = receive(brokerSocket); // receive the acknowledgment for the finishing of the payword requests
		
		send(brokerSocket,Command.goodbyeFromVendor);
		message = receive(brokerSocket); // receive the goodbye from broker
		logger.info("End sending!");
	}

	public void startWatchdogProcess()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				String signalFilePath = "resources/signal.txt";
				FileReader fr;
				BufferedReader br;
				try 
				{
					fr = new FileReader(new File(signalFilePath));
					br = new BufferedReader(fr);
					String line = br.readLine();

					if(line.contains("0"))
					{
						Vendor.this.logger.info("watchdog process - start redeem paywords");
						Vendor.this.redeemPaywords();
						Vendor.this.logger.info("watchdog process - end redeem paywords");
					}

					Thread.sleep(5000);

					br.close();
					fr.close();
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
			}
		}).start();
	}
	
	@Override
	public void onReceiveIncomingConnection(Socket hostSocket)
	{
		/*
		 * Incoming requests will occur only from Users (clients).
		 */
		boolean isFinished = false;

		boolean isCommitmentExpired     = false;
		boolean isCommitmentValueEnough = true;
		
		Integer identityNumber = null;
		
		String message   = "";
		String command   = "";
		String arguments = "";
		Receipt receipt = new Receipt();
		Commitment commitment = null;

		while(!isFinished)
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
					case Command.helloFromUser :
						{
							ServentIdentity userIdentity = ServentIdentity.decode(arguments);
							identityMap.put(userIdentity.getIdentityNumber(), userIdentity);
							identityNumber = userIdentity.getIdentityNumber();
							send(hostSocket, Command.helloFromVendor + Command.sep + getOwnIdentity().encode());
						}
						break;
					case Command.productsCatalogueRequest :
						send(hostSocket, Command.productsCatalogueOffer + Command.sep + getCatalogue());
						break;
					case Command.productReservationRequest :
						receipt.addProduct(catalogue.get(Integer.valueOf(arguments)));
						send(hostSocket, Command.productReservationAccepted);
						// if we do not have enough quantity of it..
						// send(hostSocket, Command.productReservationRejected);
						break;
					case Command.receiptRequest :
						send(hostSocket, Command.receiptOffer + Command.sep + receipt.getTotalAmount());
						break;
					case Command.receiptAcknowleged :
						{
							if(commitmentMap.containsKey(identityNumber))
							{
								if (isCommitmentExpired)
									send(hostSocket, Command.commitmentExpiredRequestNewOne);
								else if(!isCommitmentValueEnough)
									send(hostSocket, Command.commitmentValueNotEnoughRequestAdditionalOne + Command.sep + " ");
								else
								{
									commitment = commitmentMap.get(identityNumber);
									send(hostSocket, Command.commitmentPaywordRequest);
								}
							}
							else
								send(hostSocket, Command.commitmentRequest);
						}
						break;
					case Command.commitmentOffer :
						{		
							commitment = Commitment.decode(arguments.substring(0, arguments.lastIndexOf(" ")));
							// check the signature of the Certificate. 
							// This requires that the Vendor obtains the brokers
							// public key in a previous step
							if(commitment.isSignatureAuthentic(commitment.getUserCertificate().getUserEncodedPublicKey()))
							{
								commitmentMap.put(identityNumber, commitment);
								send(hostSocket, Command.commitmentPaywordRequest);
							}
							else
							{
								send(hostSocket, Command.commitmentRejectedReceiptAborted + Command.sep + "Commitment not accepted!");
								receipt.clear();
							}
						}
					break;
					case Command.commitmentPaywordOffer :
						{
							String  paywordValue = arguments.substring(1, arguments.lastIndexOf(","));
							Integer paywordIndex = Integer.valueOf(arguments.substring(arguments.lastIndexOf(",") + 1, arguments.indexOf(")")));
							if(commitment.isPaywordValid(paywordValue, paywordIndex))
							{
								Double amountReceived = commitment.processPayword(paywordValue, paywordIndex);
			
								if(receipt.coversCost(amountReceived))
									send(hostSocket, Command.commitmentPaywordAcceptedReceiptFinalized + Command.sep + receipt);
								else
									send(hostSocket, Command.commitmentPaywordExceedsReceiptAborted);
							}
							else
							{
								send(hostSocket, Command.commitmentPaywordRejectedReceiptAborted + Command.sep + "Everything is aborted");
								receipt.clear();
							}
						}
						break;
					case Command.goodbyeFromUser:
						send(hostSocket, Command.goodbyeFromVendor);
						isFinished  = true;
						break;
					default:
						send(hostSocket, Command.commandError);
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
		ServentIdentity brokerIdentity = new ServentIdentity(
				PaywordConfiguration.BROKER_IDENTITY_NUMBER, 
				PaywordConfiguration.BROKER_IP_ADDRESS, 
				PaywordConfiguration.BROKER_PORT_NUMBER);
		Vendor broker = new Vendor(brokerIdentity);
		broker.start();
	}

}
