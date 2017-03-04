package app.payword;

import java.net.Socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import app.payword.network.Protocol;
import app.payword.network.Servent;

public class Vendor extends Servent
{
	private final String VENDOR_DESCRIPTION = "We are a global company that sells tacos";
	
	public Vendor()
	{
		super(Logger.getLogger("Vendor"), "300", "127.0.0.3", 6799);
	}

	@Override
	public void onReceiveIncomingConnection(Socket hostSocket)
	{
		/*
		 * Incoming requests will occur only from Users (clients).
		 */
		boolean closingSignal = false;

		boolean isCommitmentAvailable   = true;
		boolean isCommitmentExpired     = false;
		boolean isCommitmentValueEnough = true;
		boolean everythingIsOk = true;

		String message   = "";
		String command   = "";
		String arguments = "";
		
		while(!closingSignal)
		{
			try
			{
				message = receive(hostSocket);
				if(message.contains(" "))
				{
					command   = message.substring(0, message.indexOf(" "));
					arguments = message.substring(message.indexOf(" ") + 1);
				}
				else
					command = message;
				logger.info(message);
				switch (command)
				{
					case Protocol.Command.helloFromUser :
						send(hostSocket, Protocol.Command.helloFromVendor + Protocol.Command.SEPARATOR + VENDOR_DESCRIPTION);
						break;
					case Protocol.Command.productsCatalogueRequest :
						send(hostSocket, Protocol.Command.productsCatalogueOffer);
						break;
					case Protocol.Command.productReservationRequest :
						send(hostSocket, Protocol.Command.productReservationAccepted);
						// send(hostSocket, Protocol.Command.productReservationRejected);
						break;
					case Protocol.Command.receiptRequest :
						send(hostSocket, Protocol.Command.receiptOffer);
						break;
					case Protocol.Command.receiptAcknowleged :
						if(!isCommitmentAvailable)
							send(hostSocket, Protocol.Command.commitmentRequest);
						else if (isCommitmentExpired)
							send(hostSocket, Protocol.Command.commitmentExpiredRequestNewOne);
						else if(!isCommitmentValueEnough)
							send(hostSocket, Protocol.Command.commitmentValueNotEnoughRequestAdditionalOne + Protocol.Command.SEPARATOR + "money needed more");
						else
							send(hostSocket, Protocol.Command.commitmentChainRingRequest + Protocol.Command.SEPARATOR + "Products"); // !! Attention, next step must complete with success otherwise ABORTbreak;
						break;
					case Protocol.Command.commitmentOffer :
						if(everythingIsOk) // here we may reject forgered commitments
							send(hostSocket, Protocol.Command.commitmentChainRingRequest + Protocol.Command.SEPARATOR + "Products"); // !! Attention, next step must complete with success otherwise ABORTbreak;
						else
							send(hostSocket, Protocol.Command.commitmentRejectedReceiptAborted);
						break;
					case Protocol.Command.commitmentChainRingOffer :
						if(everythingIsOk)
							send(hostSocket, Protocol.Command.commitmentChainRingAcceptedReceiptFinalized + Protocol.Command.SEPARATOR + "Products");
						else
							send(hostSocket, Protocol.Command.commitmentChainRingRejectedReceiptAborted + Protocol.Command.SEPARATOR + "Everything is aborted");
						break;
					case Protocol.Command.goodbyeFromUser:
						send(hostSocket, Protocol.Command.goodbyeFromVendor);
						break;
					default:
						send(hostSocket, Protocol.Command.commandError);
						break;
				}
			} catch( Exception e)
			{
				logger.error(e);
				logger.info("No further messages will be exchanged!");
				return;
			}
		}
	}
	
	public static void main(String[] args)
	{
		BasicConfigurator.configure();
		Vendor broker = new Vendor();
		broker.start();
	}

}
