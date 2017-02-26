package app.payword.network;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.apache.log4j.Logger;


/*
 * SERVer and cliENT
 */
public abstract class Servent extends Thread
{
	private String publicKey;
	private String privateKey;
	
	private ServerSocket passiveSocket;
	private Socket activeSocket;
	
	private final String ipAddress;
	private final int port;

	public final Logger logger;
	
	public Servent(Logger logger, String ipAddress, int port)
	{
		this.logger = logger;
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public abstract void onReceiveIncomingConnection(Socket client);
	
	private void safeCloseConnection(Socket client)
	{
		// This method must guarantee closing !
	}
	
	@Override
	public void run()
	{
		startListening();
	}
	
	private void startListening()
	{
		boolean closingSignal = false;
		try
		{
			passiveSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		while(closingSignal == false)
		{
			try
			{
				Socket activeSocket = passiveSocket.accept();
				Servent.this.logger.info("Connection " + activeSocket.getPort() + "/" + activeSocket.getInetAddress().getHostAddress() + " is established");
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						Servent.this.logger.info("Connection " + activeSocket.getPort() + "/" + activeSocket.getInetAddress().getHostAddress() + " is being handled");						
						Servent.this.onReceiveIncomingConnection(activeSocket);
						Servent.this.logger.info("Connection " + activeSocket.getPort() + "/" + activeSocket.getInetAddress().getHostAddress() + " is safe closed");
						Servent.this.safeCloseConnection(activeSocket); // FIXME : Razvan
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
//			output.close();
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
//			input.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return message;
	}

	protected Socket connectToServant(ServentInformation serventInformation, int numberOfAttempts)
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

	public String getPublicKey()
	{
		return publicKey;
	}

	public String getPrivateKey()
	{
		return privateKey;
	}

}
