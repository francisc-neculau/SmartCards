package app.payword;

import java.sql.Date;

public class Certificate
{
	private String brokerIdentity;
	private String brokerPublicKey;
	
	private String userIdentity;
	private String userPublicKey;
	private String userIpAddress;
	private String creditCardNumber;
	
	private Date expirationDate;

	public Certificate(String brokerIdentity,String brokerPublicKey,String userIdentity,String userPublicKey,String userIpAddress,String creditCardNumber,Date expirationDate)
	{
		this.brokerIdentity = brokerIdentity;
		this.brokerPublicKey = brokerPublicKey;

		this.userIdentity = userIdentity;
		this.userPublicKey = userPublicKey;
		this.userIpAddress = userIpAddress;
		this.creditCardNumber = creditCardNumber;

		this.expirationDate = expirationDate;
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

	public Date getExpirationDate()
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
		sb.append(" ");
		return sb.toString();
	}
}
