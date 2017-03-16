package app;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.util.Map;

import app.payword.crypto.CryptoFacade;
import app.payword.model.Certificate;
import app.payword.network.ServentIdentity;

public class Main
{

	public static void main(String[] args)
	{
//		Certificate c = Certificate.decode("100-8301505162514647866874465926845457452488156711667391817295188749879281714646088364191102921603810818860131046710157345746543582238732705944429859941218809x65537-200-7641533721694110743511306529296810239848287871312827801133451148215189823432053531808547732126450353186898016861011837518549928718947733810243438152629041x65537-127.0.0.1-4412 1234 0099 2134-20170304141454");
//		RSAPrivateKey pk = 
//		String signature = cf.generateCryptographicSignature(message, (PrivateKey)keys.get(CryptoFacade.PRIVATE_KEY));

		String rawMessage = "100-8301505162514647866874465926845457452488156711667391817295188749879281714646088364191102921603810818860131046710157345746543582238732705944429859941218809x65537-200-7641533721694110743511306529296810239848287871312827801133451148215189823432053531808547732126450353186898016861011837518549928718947733810243438152629041x65537-127.0.0.1-4412 1234 0099 2134-20170304141454";
		
		//rawMessage = "Hello World";
		
		CryptoFacade cf = CryptoFacade.getInstance();
//		Map<String, RSAKey> keys = cf.generateRsaKeys();
//		
//		String messageHash = cf.generateHash(rawMessage);
//		System.out.println(rawMessage);
//		System.out.println(cf.generateHash(rawMessage));
//		
//		String signature = cf.generateCryptographicSignature(messageHash, (PrivateKey)keys.get(CryptoFacade.PRIVATE_KEY));
//		System.out.println(signature);
//		
//		String encodedKey = CryptoFacade.getInstance().encodePublicKey((PublicKey)keys.get(CryptoFacade.PUBLIC_KEY));
//		
//		if(cf.isSignatureAuthentic(messageHash, signature, CryptoFacade.getInstance().decodePublicKey(encodedKey)))
//			System.out.println("Yes!");
//		else
//			System.out.println("No!");
//		System.out.println("----------");
		
		Map<String, RSAKey> userKeys   = cf.generateRsaKeys();
		Map<String, RSAKey> brokerKeys = cf.generateRsaKeys();
		
		ServentIdentity brokerIdentity = new ServentIdentity(100, "127.0.0.2", 6790, 
				(PublicKey)brokerKeys.get(CryptoFacade.PUBLIC_KEY));
		ServentIdentity userIdentity   = new ServentIdentity(200, "127.0.0.1", 6767, 
				(PublicKey)userKeys.get(CryptoFacade.PUBLIC_KEY));
		
//		Certificate certificate = new Certificate(brokerIdentity, userIdentity, "809", "9807");
//		String certificateSignature = certificate.generateSignature(
//					(PrivateKey)brokerKeys.get(CryptoFacade.PRIVATE_KEY)
//				);
//		String encoded = certificate.encode();
//		
//		
//		Certificate decoded = Certificate.decode(encoded);
//		
//		if(cf.isSignatureAuthentic(certificate.generateHash(), certificateSignature, (PublicKey)brokerKeys.get(CryptoFacade.PUBLIC_KEY)))
//			System.out.println("Yes!");
//
//		if(Certificate.isSignatureAuthentic(decoded, certificateSignature, decoded.getBrokerEncodedPublicKey()))
//			System.out.println("Certificate is authentic!");
//		else
//			System.out.println("Certificate is not authentic!");
		
		
//		byte b = -128;
//		System.out.println(cf.fromByteToFormattedByteString(b));
//		System.out.println(cf.fromFormattedByteStringToByte(cf.fromByteToFormattedByteString(b)));
		
	}

}
