package com.power4j.fist.sde.core;

/**
 * 定义服务端读取请求体时的 SDE 处理模式。
 */
public enum SecureInputMode {

	/**
	 * 继承上级注解或策略中的请求体模式。
	 */
	INHERIT,

	/**
	 * 不读取 SDE envelope，请求体按普通 body 处理。
	 */
	DISABLED,

	/**
	 * 请求体可以是 SDE envelope，也可以是普通 body。
	 */
	OPTIONAL,

	/**
	 * 请求体必须是 SDE envelope。
	 */
	REQUIRED,

	/**
	 * 请求体保持明文，但仍可用于响应跟随等组合模式。
	 */
	PLAIN

}
