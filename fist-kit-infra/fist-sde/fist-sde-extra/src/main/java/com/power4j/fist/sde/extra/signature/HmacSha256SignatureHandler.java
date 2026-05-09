package com.power4j.fist.sde.extra.signature;

import com.power4j.fist.sde.core.exception.SecureSignatureException;
import com.power4j.fist.sde.core.signature.SignContext;
import com.power4j.fist.sde.core.signature.SignatureHandler;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 标准版 HMAC-SHA256 签名实现。
 * <p>
 * 签名结果使用 URL-safe Base64 编码，验签时按常量时间比较结果。
 */
public class HmacSha256SignatureHandler implements SignatureHandler {

	@Override
	public byte[] sign(byte[] input, SignContext context) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(context.getKey().getEncoded(), "HmacSHA256"));
			return Base64.getUrlEncoder().withoutPadding().encode(mac.doFinal(input));
		}
		catch (Exception ex) {
			throw new SecureSignatureException("HMAC-SHA256 sign failed", ex);
		}
	}

	@Override
	public boolean verify(byte[] input, byte[] signature, SignContext context) {
		byte[] expected = sign(input, context);
		return MessageDigest.isEqual(expected, signature);
	}

}
