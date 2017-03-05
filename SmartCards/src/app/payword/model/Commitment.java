package app.payword.model;

import java.security.PrivateKey;
import java.util.List;

import app.payword.crypto.CryptoFacade;

public class Commitment
{
	private Integer vendorIdentityNumber;
	private Certificate userCertificate;
	private String hashChainRoot;
	private String currentDate;
	private Integer hashChainLength;
	private Double  chainRingValue;

	private String  lastPaywordValue;
	private Integer lastPaywordIndex;

	public Commitment(Integer vendorIdentityNumber, Certificate userCertificate, String hashChainRoot, String currentDate, Integer hashChainLength, Double chainRingValue)
	{
		this.vendorIdentityNumber = vendorIdentityNumber;
		this.userCertificate = userCertificate;
		this.hashChainRoot   = hashChainRoot;
		this.currentDate     = currentDate;
		this.hashChainLength = hashChainLength;
		this.chainRingValue  = chainRingValue;
		
		this.lastPaywordValue = hashChainRoot;
		this.lastPaywordIndex = 0;
	}

	public Double processPayword(String payword, Integer index)
	{
		Double result = 0.0;
		result = (index - lastPaywordIndex) * chainRingValue;
		this.lastPaywordValue = payword;
		this.lastPaywordIndex = index;
		return result;
	}
	
	public boolean isPaywordValid(String payword, Integer index)
	{
		if(index <= lastPaywordIndex)
		{
			return false;
		}
		else if(index == lastPaywordIndex + 1)
		{
			if(!CryptoFacade.getInstance().generateHashChain(payword, 1).get(0).equals(lastPaywordValue))
				return false;
		}
		else
		{
			List<String> missingHashChain = CryptoFacade.getInstance().generateHashChain(payword, (index - lastPaywordIndex));
			if(!missingHashChain.get(0).equals(lastPaywordValue))
				return false;
		}
		return true;
	}
	
	public static boolean isSignatureAuthentic(Commitment commitment, String signature, String encodedPublicKey)
	{
		boolean isAuthentic = CryptoFacade.getInstance().isSignatureAuthentic(commitment.generateHash(), signature, CryptoFacade.decodePublicKey(encodedPublicKey));
		return isAuthentic;	
	}

	public String generateSignature(PrivateKey privateKey)
	{
		String signature = CryptoFacade.getInstance().generateCryptographicSignature(generateHash(), privateKey);
		return signature;
	}

	public String generateHash()
	{
		String hash = CryptoFacade.getInstance().generateHash(this.encode());
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
		
		Commitment decoded = new Commitment(vendorIdentityNumber, userCertificate, hashChainRoot, currentDate, hashChainLength, chainRingValue);
		
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
	
	@Override
	public String toString()
	{
		return (vendorIdentityNumber + " " + userCertificate + " " + hashChainRoot + " " + currentDate + " " + hashChainLength + " " + chainRingValue);
	}
}
