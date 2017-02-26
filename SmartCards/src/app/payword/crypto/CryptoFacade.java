package app.payword.crypto;

import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Map;

public class CryptoFacade
{

	/*
	 * RSA signature over SHA-1 message
	 */
	public String generateSignature(String message, RSAPublicKey publicKey, RSAPrivateKey privateKey)
	{
		/*
		 * s = md mod n
		 */
		return message;
	}
	
	/*
	 * m = se mod n --> deci trebuie cheia publica
	 */
	public boolean isSignatureAuthentic(String signature, String message, RSAPublicKey publicKey)
	{
		return false;
	}

	/*
	 * SHA-1
	 */
	public String generateHash(String... pieces)
	{
		return null;
	}
	
	public List<String> generateHashChain(String startingSeed)
	{
		return null;
	}

	/*
	 * Not fucking sure
	 */
	public String generateRandom(String seed)
	{
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

}
