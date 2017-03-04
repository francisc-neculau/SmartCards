package app.payword;

public class Commitment
{
	private String vendorIdentityNumber;
	private Certificate userCertificate;
	private String hashChainRoot;
	private String currentDate;
	private String hashChainLength;
	private String chainRingValue;

	public Commitment(String vendorIdentityNumber, Certificate userCertificate, String hashChainRoot, String currentDate, String hashChainLength, String chainRingValue)
	{
		this.vendorIdentityNumber = vendorIdentityNumber;
		this.userCertificate = userCertificate;
		this.hashChainRoot = hashChainRoot;
		this.currentDate = currentDate;
		this.hashChainLength = hashChainLength;
		this.chainRingValue = chainRingValue;
	}

	public String encode()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(vendorIdentityNumber);
		sb.append("--");
		sb.append(userCertificate.encode());
		sb.append("--");
		sb.append(hashChainRoot);
		sb.append("--");
		sb.append(currentDate);
		sb.append("--");
		sb.append(hashChainLength);
		sb.append("--");
		sb.append(chainRingValue);
		return sb.toString();
	}
	
	public static Commitment decode(String encoded)
	{
		String [] pieces = encoded.split("--");
		
		String vendorIdentityNumber = pieces[0];
		Certificate userCertificate = Certificate.decode(pieces[1]);
		String hashChainRoot        = pieces[2];
		String currentDate          = pieces[3];
		String hashChainLength      = pieces[4];
		String chainRingValue       = pieces[5];
		
		Commitment decoded = new Commitment(vendorIdentityNumber, userCertificate, hashChainRoot, currentDate, hashChainLength, chainRingValue);
		
		return decoded;
	}

	public String getVendorIdentityNumber()
	{
		return vendorIdentityNumber;
	}

	public Certificate getUserCertificate()
	{
		return userCertificate;
	}

	public String getHashChainRoot()
	{
		return hashChainRoot;
	}

	public String getCurrentDate()
	{
		return currentDate;
	}

	public String getHashChainLength()
	{
		return hashChainLength;
	}

	public String getChainRingValue()
	{
		return chainRingValue;
	}
	
	@Override
	public String toString()
	{
		return (vendorIdentityNumber + " " + userCertificate + " " + hashChainRoot + " " + currentDate + " " + hashChainLength + " " + chainRingValue);
	}
}
