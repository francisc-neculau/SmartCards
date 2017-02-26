package app.payword;

import java.net.Socket;
import java.sql.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

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
		String initialMessage = receive(client);
		switch (initialMessage)
		{
		case "HELLO-User":
			handleUser(client);
			break;
		case "HELLO-Vendor":
			handleVendor(client);
			break;
		default:
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
		
		send(user, "Hello! Broker here, LOGIN please!");
		
		while(!command.equals("CLOSE"))
		{
			message = receive(user);
			if(message.contains(" "))
			{
				command   = message.substring(0, message.indexOf(" "));
				arguments = message.substring(message.indexOf(" "));
			}
			else
				command = message;

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
				send(user, "CERTIFY-REQUIREMENTS identity publicKey ipAddress");
				break;
			case "CERTIFY-REQUIREMENTS-OFFER":
				String userIdentity = "";
				String userPublicKey = "";
				String userIpAddress = "";
				String creditCardNumber = "";
				Certificate certificate = new Certificate(
						identity, 
						getPublicKey(), 
						userIdentity, 
						userPublicKey, 
						userIpAddress, 
						creditCardNumber, 
						new Date(12937107481L));
				send(user, "CERTIFICATE" + " " + certificate);
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