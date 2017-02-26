package app.payword;

import java.net.Socket;
import java.sql.Date;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.crypto.CryptoFacade;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class User extends Servent
{
	private Certificate certificate;
	private ServentIdentity brokerInformation;
	private int userIdentity;

	public User(ServentIdentity brokerInformation)
	{
		super(Logger.getLogger("User"), "127.0.0.1", 6767);
		this.brokerInformation = brokerInformation;
	}

	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		
		ServentIdentity brokerInformation = new ServentIdentity("127.0.0.2", 6790, null);
		User user = new User(brokerInformation);
		user.start();
		user.establishAccount();
	}
	
	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		/*
		 * Handle request
		 */
	}

	public void establishAccount()
	{
		Socket brokerSocket = connectToServant(brokerInformation, 4);

		send(brokerSocket, "HELLO-User");
		String message = receive(brokerSocket);
		System.out.println(message);
		
		send(brokerSocket, "LOGIN" + " " + "credentials");
		message = receive(brokerSocket);
		logger.info(message);
		
		send(brokerSocket, "CERTIFY");
		message = receive(brokerSocket);
		logger.info(message);
		
		send(brokerSocket, "CERTIFY-REQUIREMENTS-OFFER" + " -i=" + userIdentity + " -pk=" + getPublicKey() + " -ipAddr=" + getIpAddress());
		message = receive(brokerSocket);
		logger.info(message);
		
		String certificateString = message.substring(message.indexOf(" "), message.lastIndexOf(" "));
		certificate = fromStringToCertificate(certificateString);
		String certificateSignature = message.substring(message.lastIndexOf(" "));
		if(CryptoFacade.getInstance().isSignatureAuthentic(certificateSignature, message, brokerInformation.getRsaPublicKey()))
			logger.info("Certificate is authentic");
		else
			logger.info("Certificate is not authentic");
		send(brokerSocket, "CLOSE");
		message = receive(brokerSocket);
		logger.info(message);
	}
	
	public Certificate fromStringToCertificate(String certificateString)
	{
		String [] certificateStringPieces = certificateString.split(" ");
		String brokerIdentity  = certificateStringPieces[0];
		String brokerPublicKey = certificateStringPieces[1];
		
		String userIdentity     = certificateStringPieces[2];
		String userPublicKey    = certificateStringPieces[3];
		String userIpAddress    = certificateStringPieces[4];
		String creditCardNumber = certificateStringPieces[5];
		
		Date expirationDate = new Date(Long.parseLong(certificateStringPieces[6]));

		return new Certificate(brokerIdentity, brokerPublicKey, userIdentity, userPublicKey, userIpAddress, creditCardNumber, expirationDate);
	}
}
