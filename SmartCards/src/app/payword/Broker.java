package app.payword;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.model.Certificate;
import app.payword.model.Commitment;
import app.payword.network.Protocol.Command;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;
import app.payword.util.DateUtil;

public class Broker extends Servent
{
	private Map<ServentIdentity, Certificate> registeredUsers;
	private Map<String, Boolean> commitmentProcessedMap;
	private Logger logger;
	public Broker()
	{
		super("Broker", 100, "127.0.0.2", 6790);
		logger = Logger.getLogger("Broker");
		registeredUsers = new HashMap<>();
		commitmentProcessedMap = new HashMap<>();
	}
	/*
	 * TO-DO :
	 * 	- 	Implement service that runs and checks expirations of Certificates.
	 * 	  When it finds one, it tries to connect to the User and informs him that 
	 * 	  his certificate is no longer valid.
	 * 	    User must then obtain a new Certificate.
	 */
	
	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		// FIXME : must have a max time wait !
		logger.info("Waiting initial message!");
		String initialMessage = receive(client);
		logger.info(initialMessage);
		switch (initialMessage)
		{
		case Command.helloFromUser :
			handleUser(client);
			break;
		case Command.helloFromVendor:
			handleVendor(client);
			break;
		default:
			logger.info("Bad initial message!");
			break;
		}
	}

	private void handleUser(Socket user)
	{
		String message   = "";
		String command   = "";
		String arguments = "";
		
		send(user, Command.helloFromBroker);
		
		while(true)
		{
			message = receive(user);
			if(message.contains(" "))
			{
				command   = message.substring(0, message.indexOf(" "));
				arguments = message.substring(message.indexOf(" ") + 1);
			}
			else
				command = message;
			switch (command)
			{
				case Command.certificateRequest : 
					send(user, Command.certificateInformationsRequest + Command.sep + "identityNumber ipAddress portNumber publicKey");
					break;
				case Command.certificateInformationsOffer : 
					// Check the user has allready a certificate ? maybe further actions
					ServentIdentity userIdentity    = ServentIdentity.decode(arguments);
					Certificate     userCertificate = new Certificate(getOwnIdentity(), userIdentity, "4412 1234 0099 2134", DateUtil.getInstance().generateDate());
					userCertificate.generateSignature(getPrivateKey());
					
					logger.info("Received user identity : " + userIdentity.toString());
					logger.info("Generating certificate for user " + userIdentity.getIdentityNumber() + " :");
					logger.info(userCertificate.toString());
					logger.info("Signing certificate for user "    + userIdentity.getIdentityNumber() + " :");
					registeredUsers.put(userIdentity, userCertificate);

					send(user, Command.certificateOffer + Command.sep + userCertificate.encode());
					logger.info("Certificate and signature are sent for user " + userIdentity.getIdentityNumber());
					break;
				case Command.goodbyeFromUser :
					send(user, Command.goodbyeFromBroker);
					return;
				default:
					send(user, Command.commandError);
					break;
			}
		}
	}
	
	private void handleVendor(Socket vendor)
	{
		// FIXME : define some sort of timer for expiration of session
		// FIXME : check for bad formatted messages
		String message   = "";
		String command   = "";
		String arguments = "";
		
		send(vendor, Command.helloFromBroker);
		
		while(true)
		{
			message = receive(vendor);
			if(message.contains(" "))
			{
				command   = message.substring(0, message.indexOf(" "));
				arguments = message.substring(message.indexOf(" ") + 1);
			}
			else
				command = message;
//					logger.info("received : " + message);
			switch (command)
			{
				case Command.paywordRedeemRequest:
					send(vendor, Command.paywordRedeemAccept);
					break;
				
				/*
				 * 	Receive the paywords from the Vendor
				 */
				case Command.paywordSendReceipt:
					String[] receiptArguments = arguments.split(Command.sep);
					
					
					Commitment commitment = Commitment.decode(receiptArguments[0]); 
					if(!commitmentProcessedMap.containsKey(receiptArguments[1]) )
					{
						if(commitment.isPaywordValid(receiptArguments[1], Integer.parseInt(receiptArguments[2])))
						{
							commitmentProcessedMap.put(receiptArguments[1], true);
						}
					}
					break;
					
				case Command.paywordSendReceiptEndSignal:
					send(vendor, Command.paywordAcknowlewdgeReceiptEndSignal);
					break;
					
				case Command.goodbyeFromVendor:
					send(vendor,Command.goodbyeFromBroker);
					return;
				
				default:
					send(vendor, Command.commandError);
					break;
			}
		}
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Broker broker = new Broker();
		broker.start();
	}

}