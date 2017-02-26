package app.payword.network;

public class ServentInformation
{
	private int port;
	private String ipAddress;

	public ServentInformation(String ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
	}

	public int getPort()
	{
		return port;
	}
	public void setPort(int port)
	{
		this.port = port;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}
	public void setIpAddress(String ipAddress)
	{
		this.ipAddress = ipAddress;
	}
}
