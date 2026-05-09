package com.power4j.fist.sde.core.json;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

/**
 * SDE Web 响应体序列化契约。
 * <p>
 * 服务端在 ResponseBodyAdvice 中先通过该接口将业务返回值序列化为明文字节，再交给后续加密和 envelope 编码流程。
 */
public interface SecureJsonCodec {

	/**
	 * 将业务对象序列化为明文字节。
	 * @param value 业务对象；允许为 {@code null}
	 * @param valueType 业务对象声明类型；允许为 {@code null}
	 * @return 序列化后的明文字节
	 */
	byte[] serialize(@Nullable Object value, @Nullable Type valueType);

}
