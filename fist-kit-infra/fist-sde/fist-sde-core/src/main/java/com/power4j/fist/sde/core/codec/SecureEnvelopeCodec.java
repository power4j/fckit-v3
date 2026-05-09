package com.power4j.fist.sde.core.codec;

import com.power4j.fist.sde.core.SecureEnvelope;
import org.jspecify.annotations.Nullable;

/**
 * SDE envelope 编解码契约。
 * <p>
 * 实现只负责协议对象和传输表示之间的转换，不执行验签、重放校验、加密或解密。
 */
public interface SecureEnvelopeCodec {

	/**
	 * 将输入字节解码为 envelope 协议对象。
	 * @param input 输入字节
	 * @param context 编码上下文；为 {@code null} 时实现可使用默认上下文
	 * @return envelope 协议对象
	 */
	SecureEnvelope decode(byte[] input, @Nullable SecureEnvelopeContext context);

	/**
	 * 将 envelope 协议对象编码为字节。
	 * @param envelope envelope 协议对象
	 * @param context 编码上下文；为 {@code null} 时实现可使用默认上下文
	 * @return 编码后的字节
	 */
	byte[] encodeToBytes(SecureEnvelope envelope, @Nullable SecureEnvelopeContext context);

	/**
	 * 将 envelope 协议对象编码为适合消息转换器继续处理的 body 对象。
	 * @param envelope envelope 协议对象
	 * @param context 编码上下文；为 {@code null} 时实现可使用默认上下文
	 * @return body 对象
	 */
	Object encodeToBody(SecureEnvelope envelope, @Nullable SecureEnvelopeContext context);

}
