package app.payword;

import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

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

	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Broker broker = new Broker();
		broker.start();
	}

	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		// FIXME : must have a max time wait !
		logger.info("Waiting initial message!");
		String initialMessage = receive(client);
		logger.info(initialMessage);
		switch (initialMessage)
		{
		case "HELLO-User":
			handleUser(client);
			break;
		case "HELLO-Vendor":
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
		
		send(user, "HELLO broker here, LOGIN please!");
		
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
				case "LOGIN" :
					send(user, "LOGIN-SUCCESS" + " " + getOwnIdentity().getEncodedPublicKey());
					break;
				case "":
					break;
				case "GET-CERTIFY":
					send(user, "CERTIFY-REQUIREMENTS identityNumber ipAddress portNumber publicKey");
					break;
				case "CERTIFY-REQUIREMENTS-OFFER":
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

					send(user, "CERTIFICATE" + " " + userCertificate.getEncodedCertificate() + " " + certificateSignature);
					logger.info("Certificate and signature are sent for user " + userIdentity.getIdentityNumber());
					break;
				case "CLOSE":
					send(user, "CLOSE-OK");
					break;
				default:
					send(user, "ERROR command not recognised");
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

}