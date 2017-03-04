package app.payword;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.network.Protocol;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class User extends Servent
{
	private Certificate certificate;
	private ServentIdentity brokerIdentity; // this identity should be validated
	private ServentIdentity vendorIdentity; // by some sort of security provider

	public User(ServentIdentity brokerIdentity, ServentIdentity... vendorsIdentities)
	{
		super(Logger.getLogger("User"), "200", "127.0.0.1", 6767);
		this.brokerIdentity = brokerIdentity;
		this.vendorIdentity = vendorsIdentities[0];
	}

	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		/*
		 * Handle request
		 */
	}

	public void obtainCertificate()
	{
		Socket brokerSocket = connectToServant(brokerIdentity);
		String message      = "";

		send(brokerSocket, Protocol.Command.helloFromUser);
		message = receive(brokerSocket);


		send(brokerSocket, Protocol.Command.certificateRequest);
		message = receive(brokerSocket);
		logger.info("Requesting certificate..");


		send(brokerSocket,Protocol.Command.certificateInformationsOffer + Protocol.Command.SEPARATOR + ServentIdentity.encodeServentIdentity(getOwnIdentity()));
		message = receive(brokerSocket);
		logger.info("Certificate received (serialized form).");
		logger.info("Deserializing Certificate..");
		
		String encodedCertificate   = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
		String certificateSignature = message.substring(message.lastIndexOf(" ") + 1);
		certificate = Certificate.decode(encodedCertificate);
		brokerIdentity.setPublicKey(ServentIdentity.decodePublicKey(certificate.getBrokerEncodedPublicKey()));
		
		logger.info("Certificate : " + certificate);
		logger.info("Signature   : " + certificateSignature);
		logger.info("Checking the certificate signature..");
		if(Certificate.isCertificateAuthentic(certificate, certificateSignature))
			logger.info("Certificate is authentic!");
		else
			logger.info("Certificate is not authentic!");


		send(brokerSocket, Protocol.Command.goodbyeFromUser);
		message = receive(brokerSocket);
		logger.info(message);

		disconnectFromServant(brokerSocket);
	}

	public void simulatePayment()
	{
		Socket vendorSocket = connectToServant(brokerIdentity);
		String message = "";

		send(vendorSocket, Protocol.Command.helloFromUser);
		message = receive(vendorSocket);

		send(vendorSocket, Protocol.Command.productsCatalogueRequest);
		message = receive(vendorSocket);

		send(vendorSocket, Protocol.Command.productReservationRequest);
		message = receive(vendorSocket);

		send(vendorSocket, Protocol.Command.productReservationRequest);
		message = receive(vendorSocket);
		
		send(vendorSocket, Protocol.Command.receiptRequest);
		message = receive(vendorSocket);

		send(vendorSocket, Protocol.Command.receiptAcknowleged);
		message = receive(vendorSocket);

		/*
		 * Case of first Commitment of Day
		 */
		//# Compute Commitment and send it.
		send(vendorSocket, Protocol.Command.commitmentOffer + Protocol.Command.SEPARATOR + "Commitment");
		//# Here we should receive a request for the ChainRing
		message = receive(vendorSocket);

		//# Compute the ChainRing and send it!
		send(vendorSocket, Protocol.Command.commitmentChainRingOffer + Protocol.Command.SEPARATOR + "Chain Ring");
		//# Products Received here
		message = receive(vendorSocket); 
		
		send(vendorSocket, Protocol.Command.goodbyeFromUser);
		message = receive(vendorSocket);

		disconnectFromServant(vendorSocket);
	}

	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasicConfigurator.configure();

		ServentIdentity brokerIdentity = new ServentIdentity("100", "127.0.0.2", 6790);
		ServentIdentity vendorIdentity = new ServentIdentity("300", "127.0.0.2", 6799);
		
		User user = new User(brokerIdentity, new ServentIdentity [] {vendorIdentity});
		user.start();
		user.obtainCertificate();
	}
}
