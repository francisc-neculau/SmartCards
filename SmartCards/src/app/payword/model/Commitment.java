package app.payword.model;

import java.security.PrivateKey;
import java.util.List;

import app.payword.crypto.CryptoFacade;

public class Commitment
{
	// only the below 6 will be hashed for the signature
	private Integer vendorIdentityNumber;
	private Certificate userCertificate;
	private String hashChainRoot;
	private String currentDate;
	private Integer hashChainLength;
	private Double  chainRingValue;

	private String  lastPayword;	// cl	
	private Integer lastPaywordIndex;	// l

	private String digitalSignature;

	private List<String> paywordsList;
	
	public Commitment(Integer vendorIdentityNumber, Certificate userCertificate, String hashChainRoot, String currentDate, Integer hashChainLength, Double chainRingValue)
	{
		this.vendorIdentityNumber = vendorIdentityNumber;
		this.userCertificate = userCertificate;
		this.hashChainRoot   = hashChainRoot;
		this.currentDate     = currentDate;
		this.hashChainLength = hashChainLength;
		this.chainRingValue  = chainRingValue;
		
		this.lastPayword = hashChainRoot;
		this.lastPaywordIndex = 0;
		
		// The Certificate should be signed after instantiation.
		this.digitalSignature = "";
	}

	public Double processPayword(String payword, Integer index)
	{
		Double result = 0.0;
		result = (index - lastPaywordIndex) * chainRingValue;
		setLastPaywordUsed(payword, index);
		return result;
	}
	
	public void setLastPaywordUsed(String payword, Integer index)
	{
		this.lastPayword = payword;
		this.lastPaywordIndex = index;
	}
	
	public boolean isPaywordValid(String payword, Integer index)
	{
		if(index <= lastPaywordIndex)
		{
			return false;
		}
		else if(index == lastPaywordIndex + 1)
		{
			if(!CryptoFacade.getInstance().generateHashChain(payword, 1).get(0).equals(lastPayword))
				return false;
		}
		else
		{
			List<String> missingHashChain = CryptoFacade.getInstance().generateHashChain(payword, (index - lastPaywordIndex));
			if(!missingHashChain.get(0).equals(lastPayword))
				return false;
		}
		return true;
	}
	
	public boolean isSignatureAuthentic(String encodedPublicKey)
	{
		boolean isAuthentic = CryptoFacade.getInstance().isSignatureAuthentic(this.generateHash(), this.digitalSignature, CryptoFacade.decodePublicKey(encodedPublicKey));
		return isAuthentic;	
	}

	public void generateDigitalSignature(PrivateKey privateKey)
	{
		String signature = CryptoFacade.getInstance().generateCryptographicSignature(generateHash(), privateKey);
		digitalSignature = signature;
	}

	public String generateHash()
	{
		String hash = CryptoFacade.getInstance().generateHash(this.toString());
		return hash;
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
		sb.append("--");
		sb.append(digitalSignature);
		return sb.toString();
	}
	
	public static Commitment decode(String encoded)
	{
		String [] pieces = encoded.split("--");

		Integer vendorIdentityNumber = Integer.valueOf(pieces[0]);
		Certificate userCertificate  = Certificate.decode(pieces[1]);
		String hashChainRoot    = pieces[2];
		String currentDate      = pieces[3];
		Integer hashChainLength = Integer.valueOf(pieces[4]);
		Double chainRingValue  = Double.valueOf(pieces[5]);
		String digitalSignature = pieces[6];

		Commitment decoded = new Commitment(vendorIdentityNumber, userCertificate, hashChainRoot, currentDate, hashChainLength, chainRingValue);
		decoded.setDigitalSignature(digitalSignature);

		return decoded;
	}

	public Integer getVendorIdentityNumber()
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

	public Integer getHashChainLength()
	{
		return hashChainLength;
	}

	public Double getChainRingValue()
	{
		return chainRingValue;
	}
	
	public String getLastPaywordValue() {
		return lastPayword;
	}

	public Integer getLastPaywordIndex() {
		return lastPaywordIndex;
	}
	
	private void setDigitalSignature(String digitalSignature)
	{
		this.digitalSignature = digitalSignature;
	}
	
	public List<String> getPaywordsList()
	{
		return paywordsList;
	}
	
	public void setPaywordsList(List<String> paywordsList)
	{
		this.paywordsList = paywordsList;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(vendorIdentityNumber);
		sb.append(" ");
		sb.append(userCertificate);
		sb.append(" ");
		sb.append(hashChainRoot);
		sb.append(" ");
		sb.append(currentDate);
		sb.append(" ");
		sb.append(hashChainLength);
		sb.append(" ");
		sb.append(chainRingValue);
		return sb.toString();
	}



}
