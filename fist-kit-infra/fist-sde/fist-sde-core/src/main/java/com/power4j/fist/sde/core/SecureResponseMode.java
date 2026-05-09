package com.power4j.fist.sde.core;

/**
 * 定义服务端写出响应体时的 SDE 处理模式。
 */
public enum SecureResponseMode {

	/**
	 * 继承上级注解或策略中的响应体模式。
	 */
	INHERIT,

	/**
	 * 响应体不封装为 SDE envelope。
	 */
	DISABLED,

	/**
	 * 响应体始终封装为 SDE envelope。
	 */
	ENABLED,

	/**
	 * 仅当请求使用 SDE envelope 时，响应体才封装为 SDE envelope。
	 */
	FOLLOW_REQUEST

}
