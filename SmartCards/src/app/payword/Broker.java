package app.payword;

import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.network.Protocol;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class Broker extends Servent
{
	private Map<ServentIdentity, Certificate> registeredUsers;

	public Broker()
	{
		super(Logger.getLogger("Broker"), "100", "127.0.0.2", 6790);
		registeredUsers = new HashMap<>();
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
		case Protocol.Command.helloFromUser :
			handleUser(client);
			break;
		case Protocol.Command.helloFromVendor:
			handleVendor(client);
			break;
		default:
			logger.info("Bad initial message!");
			break;
		}
		
	}

	private void handleUser(Socket user)
	{
		// FIXME : define some sort of timer for expiration of session
		// FIXME : check for bad formatted messages
		String message = "";
		String command = "";
		String arguments = "";
		
		send(user, Protocol.Command.helloFromBroker);
		
		while(!command.equals("CLOSE"))
		{
			message = receive(user);
			if(message.contains(" "))
			{
				command   = message.substring(0, message.indexOf(" "));
				arguments = message.substring(message.indexOf(" ") + 1);
			}
			else
				command = message;
			logger.info(message);
			switch (command)
			{
				case Protocol.Command.certificateRequest :
					send(user, Protocol.Command.certificateInformationsRequest + Protocol.Command.SEPARATOR + "identityNumber ipAddress portNumber publicKey");
					break;
				case Protocol.Command.certificateInformationsOffer :
					// Check the user has allready a certificate ? maybe further actions
					ServentIdentity userIdentity    = ServentIdentity.decodeServentIdentity(arguments);
					Certificate     userCertificate = new Certificate(getOwnIdentity(), userIdentity, "4412 1234 0099 2134", generateExpirationDate());
					String certificateSignature     = userCertificate.generateCryptographicSignature(getPrivateKey());
					
					logger.info("Received user identity : " + userIdentity.toString());
					logger.info("Generating certificate for user " + userIdentity.getIdentityNumber() + " :");
					logger.info(userCertificate.toString());
					logger.info("Signing certificate for user "    + userIdentity.getIdentityNumber() + " :");
					logger.info(certificateSignature);
					registeredUsers.put(userIdentity, userCertificate);

					send(user, Protocol.Command.certificateOffer + Protocol.Command.SEPARATOR + userCertificate.encode() + " " + certificateSignature);
					logger.info("Certificate and signature are sent for user " + userIdentity.getIdentityNumber());
					break;
				case Protocol.Command.goodbyeFromUser:
					send(user, Protocol.Command.goodbyeFromBroker);
					break;
				default:
					send(user, Protocol.Command.commandError);
					break;
			}
		}
	}
	
	private void handleVendor(Socket vendor)
	{
		
	}
	
	private String generateExpirationDate()
	{
		// FIXME : This should be somewhere in time 
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(ts);
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Broker broker = new Broker();
		broker.start();
	}

}