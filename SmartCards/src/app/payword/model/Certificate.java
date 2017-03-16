package app.payword.model;

import java.security.PrivateKey;

import app.payword.crypto.CryptoFacade;
import app.payword.network.ServentIdentity;

public class Certificate
{
	// only the below 7 will be hashed for the signature
	private Integer brokerIdentityNumber;
	private String brokerEncodedPublicKey;
	private Integer userIdentityNumber;
	private String userEncodedPublicKey;
	private String userIpAddress;
	private String creditCardNumber;
	private String expirationDate;

	private String digitalSignature;
	
	public Certificate(ServentIdentity brokerIdentity, ServentIdentity userIdentity, String creditCardNumber, String expirationDate)
	{
		this(brokerIdentity.getIdentityNumber(), brokerIdentity.getEncodedPublicKey(), userIdentity.getIdentityNumber(), userIdentity.getEncodedPublicKey(), userIdentity.getIpAddress(), creditCardNumber, expirationDate);
	}
	public Certificate(Integer brokerIdentityNumber,String brokerPublicKey,Integer userIdentityNumber,String userPublicKey,String userIpAddress,String creditCardNumber,String expirationDate)
	{
		this.brokerIdentityNumber   = brokerIdentityNumber;
		this.brokerEncodedPublicKey = brokerPublicKey;
		this.userIdentityNumber     = userIdentityNumber;
		this.userEncodedPublicKey   = userPublicKey;
		this.userIpAddress          = userIpAddress;
		this.creditCardNumber       = creditCardNumber;

		this.expirationDate = expirationDate;
		
		// The Certificate should be signed after instantiation.
		this.digitalSignature = "";
	}
	
	public boolean isSignatureAuthentic(String encodedPublicKey)
	{
		boolean isAuthentic = CryptoFacade.getInstance().isSignatureAuthentic(this.generateHash(), this.digitalSignature, CryptoFacade.decodePublicKey(encodedPublicKey));
		return isAuthentic;
	}
	
	public void generateSignature(PrivateKey privateKey)
	{
		String signature = CryptoFacade.getInstance().generateCryptographicSignature(generateHash(), privateKey);
		digitalSignature = signature;
	}

	public String generateHash()
	{
		String certificateHash = CryptoFacade.getInstance().generateHash(this.toString());
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
		sb.append("-");
		sb.append(digitalSignature);
		return sb.toString();
	}
	
	public static Certificate decode(String encoded)
	{
		String [] pieces = encoded.split("-");
		
		Integer brokerIdentityNumber = Integer.valueOf(pieces[0]);
		String brokerPublicKey  = pieces[1];
		Integer userIdentityNumber = Integer.valueOf(pieces[2]);
		String userPublicKey    = pieces[3];
		String userIpAddress    = pieces[4];
		String creditCardNumber = pieces[5];
		String expirationDate   = pieces[6];
		String digitalSignature = pieces[7];
		
		Certificate decoded = new Certificate(brokerIdentityNumber, brokerPublicKey, userIdentityNumber, userPublicKey, userIpAddress, creditCardNumber, expirationDate);
		decoded.setDigitalSignature(digitalSignature);
		
		return decoded;
	}

	public Integer getBrokerIdentityNumber()
	{
		return brokerIdentityNumber;
	}

	public String getBrokerEncodedPublicKey()
	{
		return brokerEncodedPublicKey;
	}

	public Integer getUserIdentityNumber()
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
	
	private void setDigitalSignature(String digitalSignature)
	{
		this.digitalSignature = digitalSignature;
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
