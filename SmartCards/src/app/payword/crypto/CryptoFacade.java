package app.payword.crypto;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CryptoFacade
{	
	private static class InstanceHolder {
	    private static final CryptoFacade instance = new CryptoFacade();
	}

	public static CryptoFacade getInstance() {
	    return InstanceHolder.instance;
	}

	/*
	 * RSA signature over SHA-1 message
	 */
	public String generateSignature(String message, RSAPrivateKey privateKey)
	{
		/*
		 * s = md mod n
		 */
		Signature signature;
		try 
		{
			signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privateKey);
			signature.update(message.getBytes("UTF-8"));
			
			byte[] signedBytes = signature.sign();
			
			return new String(signedBytes,"UTF-8");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
		} 
		catch (SignatureException e) 
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}

	   
		return message;
	}
	
	/*
	 * m = se mod n --> deci trebuie cheia publica
	 */
	public boolean isSignatureAuthentic(String signatureMessage, String message, RSAPublicKey publicKey)
	{
		try 
		{
			Signature signature = Signature.getInstance("SHA1withRSA");
			byte[] signedBytes = signatureMessage.getBytes("UTF-8");
			byte[] messageBytes = message.getBytes("UTF-8");
			
			signature.initVerify(publicKey);
			signature.update(messageBytes);
			
			return signature.verify(signedBytes);
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		} 
		catch (InvalidKeyException e) 
		{
			e.printStackTrace();
		} 
		catch (SignatureException e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}

	/*
	 * SHA-1
	 */
	public String generateHash(String... pieces)
	{
		String message 		= String.join("", pieces);
		
		try 
		{
			MessageDigest md 	= MessageDigest.getInstance("SHA-1");
			byte[] sha1hash 	= new byte[40];
		    
		    md.update(message.getBytes("UTF-8"), 0, message.length());
		    sha1hash = md.digest();
		    
		    return new String(sha1hash,"UTF-8");
		} 
		catch (NoSuchAlgorithmException e) 
		{
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	    
		return null;
	}
	
	public List<String> generateHashChain(String startingSeed, int chainLength)
	{
		List<String> hashChainList = new ArrayList<>();
		for( int index = chainLength - 1; index >= 0; index--)
		{
			if(index == chainLength - 1)
				hashChainList.add(generateHash(startingSeed));
			else
				hashChainList.add(generateHash(hashChainList.get(index - 1)));
		}
		
		Collections.reverse(hashChainList); // reverse the order of the elements
		return hashChainList;
	}

	/*
	 * Not fucking sure
	 */
	public String generateRandom(String seed)
	{
		try 
		{
			return (new SecureRandom(seed.getBytes("UTF-8"))).toString();
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	/*
	 * publicKey  - RSAPublicKey
	 * privateKey - RSAPrivateKey
	 * Cheia publica este (n, e), cheia privata este (p, q, d)
	 */
	public Map<String, RSAKey> generateRsaKeys()
	{
		return null;
	}

	/*
	 *  convert SHA-1 hash to String
	 */
	public static String convertToString(byte[] data) 
	 { 
       StringBuffer buf = new StringBuffer();
       for(byte bit: data)
       	buf.append(bit);
       
       return buf.toString();
	 }
	
	
	/*
	 *  convert a byte String into a byte array
	 */
	public static Byte[] convertStringToByteArray(String bytes)
	 {
		 List<Byte> resultList = new ArrayList<>();
		 for( int index = 0; index < bytes.length(); index++)
		 {
			 String s = "" + bytes.charAt(index);
			 resultList.add((byte) Byte.decode(s));
		 }
		 
		 return resultList.toArray(new Byte[0]);
	 }
}
