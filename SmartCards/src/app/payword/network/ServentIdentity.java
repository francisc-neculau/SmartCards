package app.payword.network;

import java.security.interfaces.RSAPublicKey;

public class ServentIdentity
{
	private int port;
	private String ipAddress;
	private RSAPublicKey rsaPublicKey;
	
	public ServentIdentity(String ipAddress, int port, RSAPublicKey rsaPublicKey)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		this.rsaPublicKey = rsaPublicKey;
	}

	public int getPort()
	{
		return port;
	}

	public String getIpAddress()
	{
		return ipAddress;
	}
	
	public RSAPublicKey getRsaPublicKey()
	{
		return rsaPublicKey;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ipAddress);
		sb.append("/");
		sb.append(port);
		return sb.toString();
	}
}
