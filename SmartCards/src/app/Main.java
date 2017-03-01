package app;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.util.Map;

import app.payword.crypto.CryptoFacade;

public class Main
{

	public static void main(String[] args)
	{
		CryptoFacade cf = CryptoFacade.getInstance();
		Map<String, RSAKey> keys = cf.generateRsaKeys();
		String message = cf.generateHash("Hello world!");
		System.out.println(cf.generateHash("Hello world!"));
		System.out.println(cf.generateHash("Hello world!"));
		String signature = cf.generateCryptographicSignature(message, (PrivateKey)keys.get(CryptoFacade.PRIVATE_KEY));
		System.out.println(signature);
		System.out.println(message);
		if(cf.isSignatureAuthentic(signature, message, (PublicKey)keys.get(CryptoFacade.PUBLIC_KEY)))
			System.out.println("Ok!");
		System.out.println();
		System.out.println("----------");
		byte b = -128;
		System.out.println(cf.fromByteToFormattedByteString(b));
		System.out.println(cf.fromFormattedByteStringToByte(cf.fromByteToFormattedByteString(b)));
	}

}
