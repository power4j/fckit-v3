package com.power4j.fist.sde.extra.signature;

import com.power4j.fist.sde.core.exception.SecureAlgorithmUnavailableException;
import com.power4j.fist.sde.core.exception.SecureSignatureException;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 国密 HMAC-SM3 签名实现。
 * <p>
 * 运行环境必须提供 {@code HmacSM3} 算法，通常由 Bouncy Castle Provider 提供。
 */
public class HmacSm3SignatureHandler implements SignatureHandler {

	@Override
	public byte[] sign(byte[] input, SignContext context) {
		try {
			Mac mac = mac();
			mac.init(new SecretKeySpec(context.getKey().getEncoded(), "HmacSM3"));
			return Base64.getUrlEncoder().withoutPadding().encode(mac.doFinal(input));
		}
		catch (SecureAlgorithmUnavailableException ex) {
			throw ex;
		}
		catch (Exception ex) {
			throw new SecureSignatureException("HMAC-SM3 sign failed", ex);
		}
	}

	@Override
	public boolean verify(byte[] input, byte[] signature, SignContext context) {
		byte[] expected = sign(input, context);
		return MessageDigest.isEqual(expected, signature);
	}

	private static Mac mac() {
		try {
			return Mac.getInstance("HmacSM3");
		}
		catch (NoSuchAlgorithmException ex) {
			throw new SecureAlgorithmUnavailableException("HMAC-SM3 algorithm unavailable", ex);
		}
	}

}
