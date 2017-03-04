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
	public static final Integer DEFAULT_NUMBER_OF_ATTEMPTS = 4;
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

	public abstract void onReceiveIncomingConnection(Socket hostSocket);

	private void safeCloseConnection(Socket hostSocket)
	{
		Servent.this.logger.info("Connection " + hostSocket.getInetAddress().getHostAddress() + "/" + hostSocket.getPort() + " is safe closing");
		try 
		{
			hostSocket.close();
		} 
		catch (IOException e) 
		{
			Servent.this.logger.warn("Closing failed for host : " + hostSocket.getInetAddress().getHostAddress() + "/" + hostSocket.getPort());
			Servent.this.logger.info("Retry to close the connection in 2 seconds..");
			try 
			{
				Thread.sleep(2000);
				safeCloseConnection(hostSocket);
			} 
			catch (InterruptedException e1) 
			{
				logger.error(e1);
			}
		}
		Servent.this.logger.info("Connection " + hostSocket.getInetAddress().getHostAddress() + "/" + hostSocket.getPort() + " is closed");
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
			logger.error(e);
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
						Servent.this.safeCloseConnection(activeSocket);
						
					}
				}).start();
			}
			catch (IOException e)
			{
				logger.error(e);
			}
			if(closingSignal)
				break; // FIXME : Handle all threads launched.. inform them that the master must close. Do not accept new connections !
		}
		try
		{
			passiveSocket.close();
		}
		catch (IOException e)
		{
			logger.error(e);
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
			logger.error(e);
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
			logger.error(e);
		}
		
		return message;
	}
	
	protected Socket connectToServant(ServentIdentity serventIdentity)
	{
		return connectToServant(serventIdentity, DEFAULT_NUMBER_OF_ATTEMPTS);
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
			} catch (IOException e)
			{
				logger.error("Establishing connection failed!", e);
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

	protected void disconnectFromServant(Socket socket)
	{
		safeCloseConnection(socket);
	}

	public RSAPublicKey getPublicKey()
	{
		return publicKey;
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
