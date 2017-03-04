package app.payword;

import java.security.PrivateKey;

import app.payword.crypto.CryptoFacade;
import app.payword.network.ServentIdentity;

public class Certificate
{
	private String brokerIdentityNumber;
	private String brokerEncodedPublicKey;
	
	private String userIdentityNumber;
	private String userEncodedPublicKey;
	private String userIpAddress;
	private String creditCardNumber;
	
	private String expirationDate;

	public Certificate(ServentIdentity brokerIdentity, ServentIdentity userIdentity, String creditCardNumber, String expirationDate)
	{
		this(brokerIdentity.getIdentityNumber(), brokerIdentity.getEncodedPublicKey(), userIdentity.getIdentityNumber(), userIdentity.getEncodedPublicKey(), userIdentity.getIpAddress(), creditCardNumber, expirationDate);
	}
	public Certificate(String brokerIdentityNumber,String brokerPublicKey,String userIdentityNumber,String userPublicKey,String userIpAddress,String creditCardNumber,String expirationDate)
	{
		this.brokerIdentityNumber = brokerIdentityNumber;
		this.brokerEncodedPublicKey      = brokerPublicKey;

		this.userIdentityNumber = userIdentityNumber;
		this.userEncodedPublicKey      = userPublicKey;
		this.userIpAddress      = userIpAddress;
		this.creditCardNumber   = creditCardNumber;

		this.expirationDate = expirationDate;
	}
	
	public String generateCryptographicSignature(PrivateKey privateKey)
	{
		String signature = CryptoFacade.getInstance().generateCryptographicSignature(getCertificateHash(), privateKey);
		return signature;
	}

	public static boolean isCertificateAuthentic(Certificate certificate, String signature)
	{
		return true;
	}
	
	public String getCertificateHash()
	{
		String certificateHash = CryptoFacade.getInstance().generateHash(this.encode());
		return certificateHash;
	}
	
	public String encode()
	{
		StringBuilder sb = new StringBuilder(); 
		sb.append(brokerIdentityNumber);
		sb.append("-");
		sb.append(brokerEncodedPublicKey);
		sb.append("-");
		sb.append(userIdentityNumber);
		sb.append("-");
		sb.append(userEncodedPublicKey);
		sb.append("-");
		sb.append(userIpAddress);
		sb.append("-");
		sb.append(creditCardNumber);
		sb.append("-");
		sb.append(expirationDate);
		return sb.toString();
	}
	
	public static Certificate decode(String encoded)
	{
		String [] pieces = encoded.split("-");
		
		String brokerIdentity   = pieces[0];
		String brokerPublicKey  = pieces[1];
		String userIdentity     = pieces[2];
		String userPublicKey    = pieces[3];
		String userIpAddress    = pieces[4];
		String creditCardNumber = pieces[5];
		String expirationDate   = pieces[6];
		
		Certificate decoded = new Certificate(brokerIdentity, brokerPublicKey, userIdentity, userPublicKey, userIpAddress, creditCardNumber, expirationDate);
		
		return decoded;
	}

	public String getBrokerIdentity()
	{
		return brokerIdentityNumber;
	}

	public String getBrokerEncodedPublicKey()
	{
		return brokerEncodedPublicKey;
	}

	public String getUserIdentity()
	{
		return userIdentityNumber;
	}

	public String getUserEncodedPublicKey()
	{
		return userEncodedPublicKey;
	}

	public String getUserIpAddress()
	{
		return userIpAddress;
	}

	public String getCreditCardNumber()
	{
		return creditCardNumber;
	}

	public String getExpirationDate()
	{
		return expirationDate;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(brokerIdentityNumber);
		sb.append(" ");
		sb.append(userIdentityNumber);
		sb.append(" ");
		sb.append(userIpAddress);
		sb.append(" ");
		sb.append(brokerEncodedPublicKey);
		sb.append(" ");
		sb.append(userEncodedPublicKey);
		sb.append(" ");
		sb.append(creditCardNumber);
		sb.append(" ");
		sb.append(expirationDate);
		return sb.toString();
	}
}
