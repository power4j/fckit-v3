package com.power4j.fist.sde.core;

/**
 * 表示 SDE 处理发生在入站还是出站方向。
 */
public enum SecureDirection {

	/**
	 * 入站方向，通常用于服务端读请求或客户端读响应。
	 */
	INBOUND,

	/**
	 * 出站方向，通常用于客户端写请求或服务端写响应。
	 */
	OUTBOUND

}
