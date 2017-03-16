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
import app.payword.util.DateUtil;
import app.payword.util.PaywordConfiguration;

public class User extends Servent
{
	private Certificate certificate;
	private ServentIdentity brokerIdentity; // this identity should be validated
	private ServentIdentity vendorIdentity; // by some sort of security provider
	
	private Map<Integer, Commitment> commitmentMap;
	private Logger logger;

	public User(ServentIdentity brokerIdentity, ServentIdentity... vendorsIdentities)
	{
		super(PaywordConfiguration.USER_LOGGER_NAME, PaywordConfiguration.USER_IDENTITY_NUMBER, PaywordConfiguration.USER_IP_ADDRESS, PaywordConfiguration.USER_PORT_NUMBER);
		this.logger = Logger.getLogger(PaywordConfiguration.USER_LOGGER_NAME);
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
		
		String encodedCertificate   = message.substring(message.indexOf(Command.sep) + 1);
		certificate = Certificate.decode(encodedCertificate);
		// FIXME : 
		// 		This should not be retrieved from the 
		// 	certificate itself but rather from an 
		// 	external provider !
		brokerIdentity.setPublicKey(ServentIdentity.decodePublicKey(certificate.getBrokerEncodedPublicKey()));
		
		logger.info("Certificate : " + certificate);
		logger.info("Checking the certificate signature..");
		if(certificate.isSignatureAuthentic(brokerIdentity.getEncodedPublicKey()))
			logger.info("Certificate is authentic!");
		else
			logger.info("Certificate is not authentic!");


		logger.info("sending : " + Command.goodbyeFromUser);
		send(brokerSocket, Command.goodbyeFromUser);
		message = receive(brokerSocket);
		logger.info("received : " + message);

		disconnectFromServant(brokerSocket);
	}

	public void simulateSingleFairPayment()
	{
		Socket vendorSocket = connectToServant(vendorIdentity);
		if(vendorSocket == null)
			return;
		String message = "";

		logger.info("--- SIMULATING SINGLE FAIR PAYMENT ---");
		
		send(vendorSocket, Command.helloFromUser + Command.sep + getOwnIdentity().encode());
		message = receive(vendorSocket);
		ServentIdentity vendorIdentity = ServentIdentity.decode(message.substring(message.indexOf(Command.sep) + 1));
		
		send(vendorSocket, Command.productsCatalogueRequest);
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.productReservationRequest + Command.sep + "0");
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.receiptRequest);
		message = receive(vendorSocket);
		Double totalAmount = Double.valueOf(message.substring(message.indexOf(Command.sep) + 1));
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
		Commitment commitment = new Commitment(vendorIdentity.getIdentityNumber(), certificate, hashChainRoot, DateUtil.getInstance().generateDate(), hashChainLength, chainRingValue);
		
		commitment.generateDigitalSignature(getPrivateKey());
		commitment.setPaywordsList(paywordsList);
		
		commitmentMap.put(vendorIdentity.getIdentityNumber(), commitment);
		
		send(vendorSocket, Command.commitmentOffer + Command.sep + commitment.encode());
		//# Here we should receive a request for the ChainRing
		message = receive(vendorSocket);

		//# Compute the ChainRing and send it!
		String payword = "(" + paywordsList.get(targetPaywordIndex) + "," + targetPaywordIndex + ")";
		send(vendorSocket, Command.commitmentPaywordOffer + Command.sep + payword);
		//# Products Received here
		message = receive(vendorSocket); 
		commitment.setLastPaywordUsed(payword, targetPaywordIndex);
		send(vendorSocket, Command.goodbyeFromUser);
		message = receive(vendorSocket);
		
		disconnectFromServant(vendorSocket);
	}

	public void simulateMultipleFairPayment()
	{
		Socket vendorSocket = connectToServant(vendorIdentity);
		if(vendorSocket == null)
			return;
		String message = "";

		Integer targetPaywordIndex;
		Double totalAmount;
		Commitment commitment;
		String payword;

		logger.info("--- SIMULATING MULTIPLE FAIR PAYMENT ---");
		
		send(vendorSocket, Command.helloFromUser + Command.sep + getOwnIdentity().encode());
		message = receive(vendorSocket);
		ServentIdentity vendorIdentity = ServentIdentity.decode(message.substring(message.indexOf(Command.sep) + 1));
		
		send(vendorSocket, Command.productsCatalogueRequest);
		message = receive(vendorSocket);
		
		/*
		 * First purchase
		 */
		send(vendorSocket, Command.productReservationRequest + Command.sep + "0");
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.productReservationRequest + Command.sep + "1");
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.receiptRequest);
		message = receive(vendorSocket);
		
		totalAmount        = Double.valueOf(message.substring(message.indexOf(Command.sep) + 1));
		targetPaywordIndex = (int) (totalAmount * 100);
		commitment = commitmentMap.get(vendorIdentity.getIdentityNumber());
		targetPaywordIndex += commitment.getLastPaywordIndex();
		
		send(vendorSocket, Command.receiptAcknowleged);
		message = receive(vendorSocket);
		
		//# Compute the ChainRing and send it!
		payword = "(" + commitment.getPaywordsList().get(targetPaywordIndex) + "," + targetPaywordIndex + ")";
		send(vendorSocket, Command.commitmentPaywordOffer + Command.sep + payword);
		//# Products Received here
		message = receive(vendorSocket); 

		
		/*
		 * Second purchase
		 */
		send(vendorSocket, Command.productReservationRequest + Command.sep + "2");
		message = receive(vendorSocket);
		
		send(vendorSocket, Command.receiptRequest);
		message = receive(vendorSocket);
		
		totalAmount        = Double.valueOf(message.substring(message.indexOf(Command.sep) + 1));
		targetPaywordIndex = (int) (totalAmount * 100);
		commitment = commitmentMap.get(vendorIdentity.getIdentityNumber());
		targetPaywordIndex += commitment.getLastPaywordIndex();
		
		send(vendorSocket, Command.receiptAcknowleged);
		message = receive(vendorSocket);
		
		//# Compute the ChainRing and send it!
		payword = "(" + commitment.getPaywordsList().get(targetPaywordIndex) + "," + targetPaywordIndex + ")";
		send(vendorSocket, Command.commitmentPaywordOffer + Command.sep + payword);
		//# Products Received here
		message = receive(vendorSocket); 
		
		send(vendorSocket, Command.goodbyeFromUser);
		message = receive(vendorSocket);
		
		disconnectFromServant(vendorSocket);
	}
	
	public void simulateForgeryPayment()
	{
		Socket vendorSocket = connectToServant(vendorIdentity);
		if(vendorSocket == null)
			return;
		//String message = "";
		
		logger.info("--- SIMULATING FORGERY PAYMENT ---");
		
		
		
		disconnectFromServant(vendorSocket);
	}
	
	public static void main(String[] args) throws InvalidKeySpecException, NoSuchAlgorithmException
	{
		BasicConfigurator.configure();

		ServentIdentity brokerIdentity = new ServentIdentity(
				PaywordConfiguration.BROKER_IDENTITY_NUMBER, 
				PaywordConfiguration.BROKER_IP_ADDRESS, 
				PaywordConfiguration.BROKER_PORT_NUMBER);
		ServentIdentity vendorIdentity = new ServentIdentity(
				PaywordConfiguration.VENDOR_IDENTITY_NUMBER, 
				PaywordConfiguration.VENDOR_IP_ADDRESS, 
				PaywordConfiguration.VENDOR_PORT_NUMBER);
		
		User user = new User(brokerIdentity, new ServentIdentity [] {vendorIdentity});
		user.start();
		user.obtainCertificate();
		
		user.simulateSingleFairPayment();
		// sleep between actions
		try
		{
			Thread.sleep(3000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		user.simulateMultipleFairPayment();
//		// sleep between actions
//		try
//		{
//			Thread.sleep(3000);
//		} catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
//		 user.simulateForgeryPayment();
		 user.end();
	}
}
