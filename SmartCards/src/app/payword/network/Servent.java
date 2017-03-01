package app.payword.network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import app.payword.crypto.CryptoFacade;


/*
 * SERVer and cliENT
 */
public abstract class Servent extends Thread
{
	private RSAPublicKey  publicKey;
	private RSAPrivateKey privateKey;

	private ServerSocket passiveSocket;
	// private Socket activeSocket; maybe maintain a list of active sockets

	private final String ipAddress;
	private final int port;

	private ServentIdentity ownIdentity;

	public final Logger logger;

	public Servent(Logger logger, String identityNumber, String ipAddress, int port)
	{
		this.logger    = logger;
		this.ipAddress = ipAddress;
		this.port      = port;

		this.logger.info("Obtaining rsa keys and identity");
		Map<String, RSAKey> keys = CryptoFacade.getInstance().generateRsaKeys();
		this.publicKey  = (RSAPublicKey)keys.get("publicKey");
		this.privateKey = (RSAPrivateKey)keys.get("privateKey");
		this.logger.info("Public Key : " + publicKey.toString());
		// FIXME: CryptoFacade.getInstance().generateRandomNumber(Integer.toString(port));
		this.logger.info("IdentityNumber : " + identityNumber);

		this.ownIdentity = new ServentIdentity(identityNumber, this.ipAddress, this.port, this.publicKey);
	}

	public abstract void onReceiveIncomingConnection(Socket client);

	private void safeCloseConnection(Socket client)
	{
		try 
		{
			client.close();
		} 
		catch (IOException e) 
		{
			Servent.this.logger.warn("Closing failed for the client : " + client.getInetAddress().getHostAddress() + "/" + client.getPort());
			Servent.this.logger.info("Retry to close the connection in 5 seconds..");
			try 
			{
				Thread.sleep(5000);
				safeCloseConnection(client);
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void run()
	{
		logger.info("Initializing services..");
		logger.info("(1/3) ipAddress/port " + ipAddress + "/" + port);
		startListening();
	}

	private void startListening()
	{
		boolean closingSignal = false;
		try
		{
			logger.info("(2/3) binding port to socket");
			passiveSocket = new ServerSocket(port);
			logger.info("(2/3) socket binded");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		logger.info("(3/3) listening started");
		logger.info("services up and running");
		while(closingSignal == false)
		{
			try
			{
				Socket activeSocket = passiveSocket.accept();
				Servent.this.logger.info("Connection " + activeSocket.getInetAddress().getHostAddress() + "/" + activeSocket.getPort() + " is established");
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						Servent.this.logger.info("Connection " + activeSocket.getInetAddress().getHostAddress() + "/" + activeSocket.getPort() + " is being handled");						
						Servent.this.onReceiveIncomingConnection(activeSocket);
						Servent.this.logger.info("Connection " + activeSocket.getInetAddress().getHostAddress() + "/" + activeSocket.getPort() + " is safe closing");
						Servent.this.safeCloseConnection(activeSocket);
						Servent.this.logger.info("Connection " + activeSocket.getInetAddress().getHostAddress() + "/" + activeSocket.getPort() + " is closed");
					}
				}).start();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if(closingSignal)
				break;
		}
		try
		{
			passiveSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return;
	}

	public void send(Socket destination, String message)
	{
		try
		{
			PrintStream output = new PrintStream(destination.getOutputStream());
			output.println(message);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public String receive(Socket source)
	{
		String message = "";
		try
		{
			Scanner input = new Scanner(source.getInputStream());
			message = input.nextLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return message;
	}

	protected Socket connectToServant(ServentIdentity serventInformation, int numberOfAttempts)
	{
		logger.info("Establishing connection to " + serventInformation + " with max number of attempts : " + numberOfAttempts);
		Socket servant = null;
		int attemptsCounter = 0;
		do
		{
			try
			{
				servant = new Socket(serventInformation.getIpAddress(), serventInformation.getPort());
				if(servant.isConnected())
				{
					logger.info("Connection established.");
					break;
				}
			} catch (UnknownHostException e)
			{
				logger.error("Establishing connection failed!", e);
				return null;
			} catch (IOException e)
			{
				logger.error("Establishing connection failed!", e);
				return null;
			}
			try
			{
				logger.info("Connection attempt " + attemptsCounter + "/" + numberOfAttempts);
				Thread.sleep(2000);
				attemptsCounter++;
			} catch (InterruptedException e)
			{
				break;
			}
		} while (attemptsCounter < numberOfAttempts);
		if(attemptsCounter - 1 == numberOfAttempts)
		{
			logger.info("Establishing connection failed!");
			return null;
		}
		return servant;
	}

	public RSAPublicKey getPublicKey()
	{
		return publicKey;
	}

	public String getEncodedPublicKey()
	{
		StringBuilder sb = new StringBuilder();
		for(Byte b : publicKey.getEncoded())
			sb.append(b.toString());
		return sb.toString();
	}
	
	public RSAPrivateKey getPrivateKey()
	{
		return privateKey;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}

	public int getPort()
	{
		return port;
	}

	public ServentIdentity getOwnIdentity()
	{
		return this.ownIdentity;
	}

}
