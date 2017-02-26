package app.payword;

import java.net.Socket;
import java.sql.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.crypto.CryptoFacade;
import app.payword.network.Servent;

public class Broker extends Servent
{
	private String identity;

	public Broker()
	{
		super(Logger.getLogger("Broker"), "127.0.0.2", 6790);
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
			//
			// Login
			//
			case "LOGIN" :
				send(user, "LOGIN-SUCCESS");
				break;
			case "":
				break;
			//
			// Certification
			//
			case "CERTIFY":
				send(user, "CERTIFY-REQUIREMENTS -i=identity -pk=publicKey -ipAddr=ipAddress");
				break;
			case "CERTIFY-REQUIREMENTS-OFFER":
				String userIdentity     = arguments.substring(arguments.indexOf("-i=") + 3, arguments.indexOf(" ", arguments.indexOf("-i=")));
				String userPublicKey    = arguments.substring(arguments.indexOf("-pk=") + 3, arguments.indexOf(" ", arguments.indexOf("-pk=")));
				String userIpAddress    = arguments.substring(arguments.indexOf("-ipAddr=") + 8);
				String creditCardNumber = "4412 1234 0099 2134";
				Certificate certificate = new Certificate(identity, getPublicKey().toString(), userIdentity, userPublicKey, userIpAddress, creditCardNumber, new Date(12937107481L));
				String certificateHash      = CryptoFacade.getInstance().generateHash(certificate.toString());
				String certificateSignature = CryptoFacade.getInstance().generateSignature(certificateHash, getPublicKey(), getPrivateKey());
				send(user, "CERTIFICATE" + " " + certificate + " " + certificateSignature);
				break;
			//
			// Closing
			//
			case "CLOSE":
				// Some safe closing functions
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

}