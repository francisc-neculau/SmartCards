package app.payword;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.crypto.CryptoFacade;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class User extends Servent
{
	private Certificate certificate;
	private ServentIdentity brokerIdentity; // this identity should be validated
											// by some sort of security provider

	public User(ServentIdentity brokerInformation)
	{
		super(Logger.getLogger("User"), "200", "127.0.0.1", 6767);
		this.brokerIdentity = brokerInformation;
	}

	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasicConfigurator.configure();
		ServentIdentity brokerInformation = new ServentIdentity("123", "127.0.0.2", 6790);
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
		Socket brokerSocket = connectToServant(brokerIdentity, 4);

		send(brokerSocket, "HELLO-User"/* + getOwnIdentity() */);
		String message = receive(brokerSocket);
		//logger.info(message);

		send(brokerSocket, "LOGIN" + " " + "credentials");
		message = receive(brokerSocket);
		String encodedPublicKey = message.substring(message.indexOf(" ") + 1);
		brokerIdentity.setPublicKey(ServentIdentity.decodePublicKey(encodedPublicKey));
		//logger.info(message);
		//logger.info("Public key of broker is received and saved! " + brokerIdentity.getPublicKey().toString());
		
		send(brokerSocket, "GET-CERTIFY");
		message = receive(brokerSocket);
		//logger.info(message);

		//logger.info("Sending own identity to the broker : " + ServentIdentity.encodeServentIdentity(getOwnIdentity()));
		send(brokerSocket,"CERTIFY-REQUIREMENTS-OFFER" + " " + ServentIdentity.encodeServentIdentity(getOwnIdentity()));
		message = receive(brokerSocket);
		//logger.info(message);

		// this stuff should go in the Certificate class. It must have te responsability to encode/decode itself
		String certificateString = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
		certificate = Certificate.decodeCertificate(certificateString);
		String certificateSignature = message.substring(message.lastIndexOf(" ") + 1);
		//logger.info("Certificate signature : " + certificateSignature);
		logger.info("Certificate hash : " + CryptoFacade.getInstance().generateHash(certificate.toString()));
		System.out.println("--"+certificate.toString()+"--");
		if (CryptoFacade.getInstance().isSignatureAuthentic(certificateSignature, certificate.getCertificateHash(), brokerIdentity.getPublicKey()))
			logger.info("Certificate is authentic");
		else
			logger.info("Certificate is not authentic");
		send(brokerSocket, "CLOSE");
		message = receive(brokerSocket);
		logger.info(message);
	}

}
