package app.payword.network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
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

	private boolean closingSignal;
	private final String ipAddress;
	private final int port;

	private ServentIdentity ownIdentity;

	private final Logger logger;

	public Servent(String childLoggerName, Integer identityNumber, String ipAddress, int port)
	{
		this.logger    = Logger.getLogger("Network-" + childLoggerName);
		this.ipAddress = ipAddress;
		this.port      = port;

		this.logger.info("Obtaining rsa keys and identity");
		Map<String, RSAKey> keys = CryptoFacade.getInstance().generateRsaKeys();
		this.publicKey  = (RSAPublicKey)keys.get("publicKey");
		this.privateKey = (RSAPrivateKey)keys.get("privateKey");
		this.logger.info("Public Key : " + publicKey.toString());
		this.logger.info("IdentityNumber : " + identityNumber);

		this.ownIdentity = new ServentIdentity(identityNumber, this.ipAddress, this.port, this.publicKey);
	}

	public abstract void onReceiveIncomingConnection(Socket hostSocket);
	
	private void safeCloseSocket(Socket hostSocket)
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
				safeCloseSocket(hostSocket);
			} 
			catch (InterruptedException e1) 
			{
				logger.error(e1);
			}
		}
		Servent.this.logger.info("Connection " + hostSocket.getInetAddress().getHostAddress() + "/" + hostSocket.getPort() + " is closed");
	}

	public void end()
	{
		try
		{
			Thread.sleep(2000);
			logger.info("Ending of servent initiated. closingSignal and closing the passiveSocket");
			for (int i = 0; i < 3; i++)
			{
				logger.info("begin in " + ((i+3)%3) + "..");
				Thread.sleep(1000);
			}
		} catch (InterruptedException e1)
		{
			e1.printStackTrace();
		}
		try
		{
			logger.info("sending closingSignal and closing the passiveSocket");
			closingSignal = false;
			passiveSocket.close();
		} catch (IOException e)
		{
			logger.error(e);
		} finally
		{
		}
	}
	
	@Override
	public void run()
	{
		logger.info("Initializing services..");
		startListening();
		logger.info("All services terminated..");
	}

	private void startListening()
	{
		logger.info("(1/3) ipAddress/port " + ipAddress + "/" + port);
		try
		{
			logger.info("(2/3) binding port to socket");
			passiveSocket = new ServerSocket(port);
			logger.info("(2/3) socket binded");
		}
		catch (IOException e)
		{
			logger.error("(2/3) socket binding failed!", e);
			return;
		}
		logger.info("(3/3) listening started");
		logger.info("services up and running");
		while(!closingSignal)
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
						Servent.this.safeCloseSocket(activeSocket);
					}
				}).start();
			}catch (SocketException e)
			{
				closingSignal = true;
				logger.error(e);
			}catch (IOException e)
			{
				// FIXME : Here we should include a max allowed number of error!
				logger.error(e);
			}
			if(closingSignal)
			{
				logger.info("closingSignal received.");
				logger.info("listening ended.");
				return;
				// FIXME : Handle all threads launched.. 
				// inform them that the master must close
				// Do not accept new connections !
			}
		}
		return;
	}

	public void send(Socket destination, String message)
	{
		try
		{
			PrintStream output = new PrintStream(destination.getOutputStream());
			output.println(message);
			logger.info("SEND to " + destination.getInetAddress().getHostAddress() + "/" + destination.getPort() + " MESSAGE< " + message + " >");
		}
		catch (IOException e)
		{
			logger.error(e);
		}
	}

	@SuppressWarnings("resource")
	public String receive(Socket source)
	{
		String message = "";
		try
		{
			Scanner input = new Scanner(source.getInputStream());
			message = input.nextLine();
			logger.info("RECEIVED from " + source.getInetAddress().getHostAddress() + "/" + source.getPort() + " MESSAGE< " + message + " >");
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
		safeCloseSocket(socket);
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
