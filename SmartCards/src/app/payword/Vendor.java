package app.payword;

import java.net.Socket;

import org.apache.log4j.Logger;

import app.payword.network.Servent;

public class Vendor extends Servent
{

	public Vendor()
	{
		super(Logger.getLogger("Vendor"), "", "", 1);
	}

	@Override
	public void onReceiveIncomingConnection(Socket client)
	{
		// TODO Auto-generated method stub
		
	}

}
