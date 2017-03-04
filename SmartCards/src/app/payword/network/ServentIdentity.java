package app.payword.network;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.payword.crypto.CryptoFacade;

public class ServentIdentity
{
	private String identityNumber;
	private int port;
	private String ipAddress;
	private PublicKey publicKey;
	
	public ServentIdentity(String identityNumber, String ipAddress, int port, PublicKey rsaPublicKey)
	{
		this(identityNumber, ipAddress, port);
		this.publicKey = rsaPublicKey;
	}

	public ServentIdentity(String identityNumber, String ipAddress, int port)
	{
		this.identityNumber = identityNumber;
		this.ipAddress      = ipAddress;
		this.port           = port;
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
	
	public PublicKey getPublicKey()
	{
		return publicKey;
	}
	
	public void setPublicKey(PublicKey publicKey)
	{
		this.publicKey = publicKey;
	}
	
	public static String encodedPublicKey(PublicKey publicKey)
	{
		return CryptoFacade.encodePublicKey(publicKey);
	}
	
	public String getEncodedPublicKey()
	{
		return CryptoFacade.encodePublicKey(publicKey);
	}
	
	public static PublicKey decodePublicKey(String encodePublicKey)
	{
		return CryptoFacade.decodePublicKey(encodePublicKey);
	}
	
	public static String encodeServentIdentity(ServentIdentity serventIdentiy)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(serventIdentiy.getIdentityNumber());
		sb.append("}{");
		sb.append(serventIdentiy.getIpAddress());
		sb.append("}{");
		sb.append(serventIdentiy.getPort());
		sb.append("}");
		if(serventIdentiy.getPublicKey() != null)
			sb.append("{" + CryptoFacade.encodePublicKey(serventIdentiy.getPublicKey()) + "}");
		return sb.toString();
	}
	
	public static ServentIdentity decodeServentIdentity(String encodedServentIdentiy)
	{
		ServentIdentity serventIdentiy = null;
		
		Pattern pattern = Pattern.compile("[A-Za-z0-9\\.]{1,}");
		Matcher matcher = pattern.matcher(encodedServentIdentiy);
		
		final List<String> matches = new ArrayList<>();
	    while (matcher.find())
	        matches.add(matcher.group(0));
	
		if(matches.size() == 4)
			serventIdentiy = new ServentIdentity(matches.get(0), matches.get(1), new Integer(matches.get(2)), CryptoFacade.decodePublicKey(matches.get(3)));
		else
			serventIdentiy = new ServentIdentity(matches.get(0), matches.get(1), new Integer(matches.get(2)));
		return serventIdentiy;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(ipAddress);
		sb.append("/");
		sb.append(port);
		sb.append("/");
		if(publicKey != null)
			sb.append(publicKey);
		else 
			sb.append("{PublicKey-Not-Set}");
		return sb.toString();
	}
}
