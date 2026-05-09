package com.power4j.fist.sde.core;

import com.power4j.fist.sde.core.exception.SecureEnvelopeException;

/**
 * 表示 SDE envelope 保护的数据范围。
 */
public enum SecureScope {

	/**
	 * 请求体。
	 */
	BODY("body"),

	/**
	 * 响应体。
	 */
	RESPONSE_BODY("responseBody");

	private final String value;

	SecureScope(String value) {
		this.value = value;
	}

	/**
	 * 获取写入 envelope 的协议值。
	 * @return 协议中的 scope 字符串
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * 将协议值解析为 scope 枚举。
	 * @param value 协议中的 scope 字符串
	 * @return 匹配的 scope
	 * @throws SecureEnvelopeException 当协议值不受支持时抛出
	 */
	public static SecureScope fromValue(String value) {
		for (SecureScope scope : values()) {
			if (scope.value.equals(value)) {
				return scope;
			}
		}
		throw new SecureEnvelopeException("unsupported scope: " + value);
	}

}
