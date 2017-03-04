package app.payword.crypto;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryptoFacade
{
	private SecureRandom random;
	public static final String PUBLIC_KEY = "publicKey";
	public static final String PRIVATE_KEY = "privateKey";
	private String globalCharset = "UTF-8";

	private static class InstanceHolder
	{
		private static final CryptoFacade instance = new CryptoFacade();
	}

	private CryptoFacade()
	{
		try
		{
			random = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}

	public static CryptoFacade getInstance()
	{
		return InstanceHolder.instance;
	}

	public String fromByteToFormattedByteString(byte b)
	{
		String bs = new Byte(b).toString();
		boolean hasSign = false;
		String result = "";
		if (bs.contains("-"))
		{
			hasSign = true;
			bs = bs.substring(1);
		}
		if (bs.length() == 1)
			result = "00" + bs;
		else if (bs.length() == 2)
			result = "0" + bs;
		else
			result = bs;
		if (hasSign)
			result = "0" + result; // negative
		else
			result = "1" + result; // positive
		return result;
	}

	public String fromBytesToFormattedBytesString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes)
			sb.append(fromByteToFormattedByteString(b));
		return sb.toString();
	}

	public byte fromFormattedByteStringToByte(String formattedByteString)
	{
		Byte result = 0;
		String sign = formattedByteString.substring(0, 1);
		formattedByteString = formattedByteString.substring(1);
		if (formattedByteString.startsWith("00"))
			result = (new Byte(formattedByteString.substring(1)).byteValue());
		else if (formattedByteString.startsWith("0"))
			result = (new Byte(formattedByteString.substring(0)).byteValue());
		else
		{
			if (sign.equals("0"))
			{// daca e negativ si e 128 biti, trebuie tranformat si returnat
				// aici ! fiindca crapa la conversia 128 pozitiv in byte
				Integer i = new Integer(formattedByteString);
				i = i * (-1);
				result = new Byte(i.byteValue());
				return result;
			} else
				result = new Byte(formattedByteString);
		}

		if (sign.equals("0"))
			return (byte) (-1 * result);
		else
			return result;
	}

	public byte[] fromFormattedBytesStringToBytes(String formattedByteString)
	{
		String byteString = "";
		byte[] bytes = new byte[formattedByteString.length() / 4];
		for (int i = 0; i <= formattedByteString.length() - 4; i = i + 4)
		{
			byteString = formattedByteString.substring(i, i + 4);
			bytes[i / 4] = fromFormattedByteStringToByte(byteString);
		}
		return bytes;
	}

	/*
	 * RSA signature over SHA-1 message
	 */
	public String generateCryptographicSignature(String message, PrivateKey privateKey)
	{
		/*
		 * s = md mod n
		 */
		Signature signature;
		try
		{
			signature = Signature.getInstance("SHA1withRSA");
			signature.initSign((RSAPrivateKey) privateKey);
			signature.update(message.getBytes(globalCharset));

			byte[] signedBytes = signature.sign();

			return fromBytesToFormattedBytesString(signedBytes);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (SignatureException e)
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
	public boolean isSignatureAuthentic(String signatureMessage, String message, PublicKey publicKey)
	{
		try
		{
			Signature signature = Signature.getInstance("SHA1withRSA");
			byte[] signedBytes = fromFormattedBytesStringToBytes(signatureMessage);// .getBytes(globalCharset);
			byte[] messageBytes = message.getBytes(globalCharset);

			signature.initVerify((RSAPublicKey) publicKey);
			signature.update(messageBytes);

			return signature.verify(signedBytes);
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (InvalidKeyException e)
		{
			e.printStackTrace();
		} catch (SignatureException e)
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
		String message = String.join("", pieces);

		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] sha1hash = new byte[40];

			md.update(message.getBytes(globalCharset), 0, message.length());
			sha1hash = md.digest();

			return fromBytesToFormattedBytesString(sha1hash);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public List<String> generateHashChain(String startingSeed, int chainLength)
	{
		List<String> hashChainList = new ArrayList<>();
		int elementCounter = 0;
		for (int index = chainLength - 1; index >= 0; index--)
		{
			if (index == chainLength - 1)
			{
				hashChainList.add(generateHash(startingSeed));
				elementCounter++;
			} else
			{
				hashChainList.add(generateHash(hashChainList.get(elementCounter - 1)));
				elementCounter++;
			}
		}

		Collections.reverse(hashChainList); // reverse the order of the elements
		return hashChainList;
	}

	/*
	 * publicKey - RSAPublicKey privateKey - RSAPrivateKey Cheia publica este
	 * (n, e), cheia privata este (p, q, d)
	 */
	public Map<String, RSAKey> generateRsaKeys()
	{
		try
		{
			Map<String, RSAKey> map = new HashMap<>();
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(512, random);

			KeyPair pair = keyGen.generateKeyPair();
			map.put(PUBLIC_KEY, (RSAKey) pair.getPublic());
			map.put(PRIVATE_KEY, (RSAKey) pair.getPrivate());

			return map;
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static RSAPublicKey decodePublicKey(String encodedPublicKey)
	{
		String[] modulusExponentPair = encodedPublicKey.split("x");
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(modulusExponentPair[0]),
				new BigInteger(modulusExponentPair[1]));

		KeyFactory keyFactory;
		try
		{
			keyFactory = KeyFactory.getInstance("RSA");
			RSAPublicKey pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
			return pubKey;
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		} catch (InvalidKeySpecException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String encodePublicKey(PublicKey publicKey)
	{
		// TODO Auto-generated method stub
		return (new String(((RSAPublicKey) publicKey).getModulus().toString()) + "x"
				+ new String(((RSAPublicKey) publicKey).getPublicExponent().toString()));
	}
}


/*
 * convert SHA-1 hash to String
 */
// public static String convertToString(byte[] data)
// {
// StringBuffer buf = new StringBuffer();
// for(byte bit: data)
// buf.append(bit);
//
// return buf.toString();
// }
/*
 * Not fucking sure
 */
// public String generateRandom(String seed)
// {
// try
// {
// return (new SecureRandom(seed.getBytes(globalCharset))).toString();
// }
// catch (UnsupportedEncodingException e)
// {
// e.printStackTrace();
// }
//
// return null;
// }
/*
 * convert a byte String into a byte array
 * 
 * public static Byte[] convertStringToByteArray(String bytes) { List<Byte>
 * resultList = new ArrayList<>(); for( int index = 0; index <
 * bytes.length(); index++) { String s = "" + bytes.charAt(index);
 * resultList.add((byte) Byte.decode(s)); }
 * 
 * return resultList.toArray(new Byte[0]); }
 */