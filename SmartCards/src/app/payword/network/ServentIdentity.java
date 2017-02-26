package app.payword.network;

import java.security.interfaces.RSAPublicKey;

public class ServentIdentity
{
	private String identityNumber;
	private int port;
	private String ipAddress;
	private RSAPublicKey rsaPublicKey;
	
	public ServentIdentity(String identityNumber, String ipAddress, int port, RSAPublicKey rsaPublicKey)
	{
		this(identityNumber, ipAddress, port);
		this.rsaPublicKey = rsaPublicKey;
	}

	public ServentIdentity(String identityNumber, String ipAddress, int port)
	{
		this.identityNumber = identityNumber;
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public String getIdentityNumber()
	{
		return  identityNumber;
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
		sb.append("/");
		sb.append(rsaPublicKey);
		return sb.toString();
	}
}
