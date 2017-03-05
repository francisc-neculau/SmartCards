package app.payword;

import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.crypto.CryptoFacade;
import app.payword.model.Certificate;
import app.payword.model.Commitment;
import app.payword.network.Protocol.Command;
import app.payword.network.Servent;
import app.payword.network.ServentIdentity;

public class User extends Servent
{
	private Certificate certificate;
	private ServentIdentity brokerIdentity; // this identity should be validated
	private ServentIdentity vendorIdentity; // by some sort of security provider
	
	private Map<Integer, Commitment> commitmentMap;
	
	public User(ServentIdentity brokerIdentity, ServentIdentity... vendorsIdentities)
	{
		super(Logger.getLogger("User"), 200, "127.0.0.1", 6767);
		this.brokerIdentity = brokerIdentity;
		this.vendorIdentity = vendorsIdentities[0];
		this.commitmentMap = new HashMap<>();
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

		send(brokerSocket, Command.helloFromUser);
		message = receive(brokerSocket);


		send(brokerSocket, Command.certificateRequest);
		message = receive(brokerSocket);
		logger.info("Requesting certificate..");


		send(brokerSocket, Command.certificateInformationsOffer + Command.sep + getOwnIdentity().encode());
		message = receive(brokerSocket);
		logger.info("Certificate received (serialized form).");
		logger.info("Deserializing Certificate..");
		
		String encodedCertificate   = message.substring(message.indexOf(" ") + 1, message.lastIndexOf(" "));
		String certificateSignature = message.substring(message.lastIndexOf(" ") + 1);
		certificate = Certificate.decode(encodedCertificate);
		// FIXME : 
		// 		This should not be retrieved from the 
		// 	certificate itself but rather from an 
		// 	external provider !
		brokerIdentity.setPublicKey(ServentIdentity.decodePublicKey(certificate.getBrokerEncodedPublicKey()));
		
		logger.info("Certificate : " + certificate);
		logger.info("Signature   : " + certificateSignature);
		logger.info("Checking the certificate signature..");
		if(Certificate.isSignatureAuthentic(certificate, certificateSignature, brokerIdentity.getEncodedPublicKey()))
			logger.info("Certificate is authentic!");
		else
			logger.info("Certificate is not authentic!");


		logger.info("sending : " + Command.goodbyeFromUser);
		send(brokerSocket, Command.goodbyeFromUser);
		message = receive(brokerSocket);
		logger.info("received : " + message);

		disconnectFromServant(brokerSocket);
	}

	public void simulatePaymentI()
	{
		Socket vendorSocket = connectToServant(vendorIdentity);
		if(vendorSocket == null)
			return;
		String message = "";

		send(vendorSocket, Command.helloFromUser + Command.sep + getOwnIdentity().encode());
		message = receive(vendorSocket);
		ServentIdentity vendorIdentity = ServentIdentity.decode(message.substring(message.indexOf(" ") + 1));
		
		send(vendorSocket, Command.productsCatalogueRequest);
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.productReservationRequest + Command.sep + "0");
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.receiptRequest);
		message = receive(vendorSocket);
		Double totalAmount = Double.valueOf(message.substring(message.indexOf(" ") + 1));
		Integer targetPaywordIndex = (int) (totalAmount * 100);
		
		send(vendorSocket, Command.receiptAcknowleged);
		message = receive(vendorSocket);
		
		
		/*
		 * Case of first Commitment of Day
		 */
		//# Compute Commitment and send it.
		List<String> paywordsList = CryptoFacade.getInstance().generateHashChain(getPrivateKey().toString(), 10000);
		String  hashChainRoot   = paywordsList.get(0);
		Integer hashChainLength = 10000;
		Double  chainRingValue  = 0.01;
		Commitment commitment = new Commitment(vendorIdentity.getIdentityNumber(), certificate, hashChainRoot, generateDate(), hashChainLength, chainRingValue);
		String signature = commitment.generateSignature(getPrivateKey());
		send(vendorSocket, Command.commitmentOffer + Command.sep + commitment.encode() + Command.sep + signature);
		//# Here we should receive a request for the ChainRing
		message = receive(vendorSocket);

		//# Compute the ChainRing and send it!
		String payword = "(" + paywordsList.get(targetPaywordIndex) + "," + targetPaywordIndex + ")";
		send(vendorSocket, Command.commitmentPaywordOffer + Command.sep + payword);
		//# Products Received here
		message = receive(vendorSocket); 

		send(vendorSocket, Command.goodbyeFromUser);
		message = receive(vendorSocket);
		
		disconnectFromServant(vendorSocket);
	}

	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasicConfigurator.configure();

		ServentIdentity brokerIdentity = new ServentIdentity(100, "127.0.0.2", 6790);
		ServentIdentity vendorIdentity = new ServentIdentity(300, "127.0.0.2", 6799);
		
		User user = new User(brokerIdentity, new ServentIdentity [] {vendorIdentity});
		user.start();
		user.obtainCertificate();
		user.simulatePaymentI();
	}
}
