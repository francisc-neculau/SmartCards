package app.payword.network;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.payword.crypto.CryptoFacade;

public class ServentIdentity
{
	private Integer   identityNumber;
	private int       port;
	private String    ipAddress;
	private PublicKey publicKey;
	
	public ServentIdentity(Integer identityNumber, String ipAddress, int port, PublicKey publicKey)
	{
		this(identityNumber, ipAddress, port);
		this.publicKey = publicKey;
	}

	public ServentIdentity(Integer identityNumber, String ipAddress, int port)
	{
		this.identityNumber = identityNumber;
		this.ipAddress      = ipAddress;
		this.port           = port;
	}
	
	public Integer getIdentityNumber()
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
	
	public String encode()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(this.getIdentityNumber());
		sb.append("}{");
		sb.append(this.getIpAddress());
		sb.append("}{");
		sb.append(this.getPort());
		sb.append("}");
		if(this.getPublicKey() != null)
			sb.append("{" + CryptoFacade.encodePublicKey(this.getPublicKey()) + "}");
		return sb.toString();
	}
	
	public static ServentIdentity decode(String encoded)
	{
		ServentIdentity serventIdentiy = null;
		
		Pattern pattern = Pattern.compile("[A-Za-z0-9\\.]{1,}");
		Matcher matcher = pattern.matcher(encoded);
		
		final List<String> matches = new ArrayList<>();
	    while (matcher.find())
	        matches.add(matcher.group(0));
	
		if(matches.size() == 4)
			serventIdentiy = new ServentIdentity(Integer.valueOf(matches.get(0)), matches.get(1), Integer.valueOf(matches.get(2)), CryptoFacade.decodePublicKey(matches.get(3)));
		else
			serventIdentiy = new ServentIdentity(Integer.valueOf(matches.get(0)), matches.get(1), Integer.valueOf(matches.get(2)));
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
