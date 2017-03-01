package app.payword;

import java.security.PrivateKey;

import app.payword.crypto.CryptoFacade;
import app.payword.network.ServentIdentity;

public class Certificate
{
	private String brokerIdentity;
	private String brokerPublicKey;
	
	private String userIdentity;
	private String userPublicKey;
	private String userIpAddress;
	private String creditCardNumber;
	
	private String expirationDate;

	public Certificate(ServentIdentity brokerIdentity, ServentIdentity userIdentity, String creditCardNumber, String expirationDate)
	{
		this(brokerIdentity.getIdentityNumber(), brokerIdentity.getEncodedPublicKey(), userIdentity.getIdentityNumber(), userIdentity.getEncodedPublicKey(), userIdentity.getIpAddress(), creditCardNumber, expirationDate);
	}
	public Certificate(String brokerIdentity,String brokerPublicKey,String userIdentity,String userPublicKey,String userIpAddress,String creditCardNumber,String expirationDate)
	{
		this.brokerIdentity = brokerIdentity;
		this.brokerPublicKey = brokerPublicKey;

		this.userIdentity = userIdentity;
		this.userPublicKey = userPublicKey;
		this.userIpAddress = userIpAddress;
		this.creditCardNumber = creditCardNumber;

		this.expirationDate = expirationDate;
	}
	
	// encodare pe biti transformati in string 1 --> 001 24 --> 024 124 ---> 124
	public String generateCryptographicSignature(PrivateKey privateKey)
	{
		String signature = CryptoFacade.getInstance().generateCryptographicSignature(getCertificateHash(), privateKey);
		return signature;
	}
	
	public String getCertificateHash()
	{
		String certificateHash = CryptoFacade.getInstance().generateHash(this.getEncodedCertificate());
		return certificateHash;
	}
	
	public String getEncodedCertificate()
	{
		return this.brokerIdentity + "-" + this.brokerPublicKey + "-" + this.userIdentity + "-" + this.userPublicKey + "-" + this.userIpAddress + "-" + this.creditCardNumber + "-" + this.expirationDate;
	}
	
	public static Certificate decodeCertificate(String encodedCertificate)
	{
		String [] pieces = encodedCertificate.split("-");
		
		String brokerIdentity = pieces[0];
		String brokerPublicKey = pieces[1];

		String userIdentity = pieces[2];
		String userPublicKey = pieces[3];
		String userIpAddress = pieces[4];
		String creditCardNumber = pieces[5];

		String expirationDate = pieces[6];
		
		Certificate result = new Certificate(brokerIdentity, brokerPublicKey, userIdentity, userPublicKey, userIpAddress, creditCardNumber, expirationDate);
		
		return result;
	}

	public String getBrokerIdentity()
	{
		return brokerIdentity;
	}

	public String getBrokerPublicKey()
	{
		return brokerPublicKey;
	}

	public String getUserIdentity()
	{
		return userIdentity;
	}

	public String getUserPublicKey()
	{
		return userPublicKey;
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
		sb.append(brokerIdentity);
		sb.append(" ");
		sb.append(userIdentity);
		sb.append(" ");
		sb.append(userIpAddress);
		sb.append(" ");
		sb.append(brokerPublicKey);
		sb.append(" ");
		sb.append(userPublicKey);
		sb.append(" ");
		sb.append(creditCardNumber);
		sb.append(" ");
		sb.append(expirationDate);
		return sb.toString();
	}
}
