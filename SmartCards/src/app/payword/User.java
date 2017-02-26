package app.payword;

import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.network.Servent;
import app.payword.network.ServentInformation;

public class User extends Servent
{
	private Certificate certificate;
	private ServentInformation brokerInformation;
	private int userIdentity;

	public User(ServentInformation brokerInformation)
	{
		super(Logger.getLogger("User"), "127.0.0.1", 6767);
		this.brokerInformation = brokerInformation;
	}

	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		
		ServentInformation brokerInformation = new ServentInformation("127.0.0.2", 6790);
		User user = new User(brokerInformation);
		user.start();
		user.obtainCertificate();
	}
	
	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		System.out.println(receive(client));
		send(client, "Hello from User");
	}

	public void obtainCertificate()
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
		
		send(brokerSocket, "CLOSE");
		message = receive(brokerSocket);
		logger.info(message);
	}
	
	public void generateNthChain()
	{
		
	}
}
