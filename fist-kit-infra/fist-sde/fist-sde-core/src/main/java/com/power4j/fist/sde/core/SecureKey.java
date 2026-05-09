package com.power4j.fist.sde.core;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * 表示 SDE 算法处理所需的密钥材料。
 * <p>
 * 密钥通过 keyRef 与策略中的密钥解析器关联，encoded 内容在构造和读取时都会复制，避免外部修改内部数组。
 */
public class SecureKey {

	private final String keyRef;

	private final String algorithm;

	private final byte[] encoded;

	/**
	 * 创建密钥对象。
	 * @param keyRef 密钥引用
	 * @param algorithm 密钥适用的算法标识
	 * @param encoded 原始密钥字节；为 {@code null} 时按空数组处理
	 */
	public SecureKey(String keyRef, String algorithm, @Nullable byte[] encoded) {
		this.keyRef = keyRef;
		this.algorithm = algorithm;
		this.encoded = encoded == null ? new byte[0] : Arrays.copyOf(encoded, encoded.length);
	}

	/**
	 * 获取密钥引用。
	 * @return 密钥引用
	 */
	public String getKeyRef() {
		return this.keyRef;
	}

	/**
	 * 获取密钥算法标识。
	 * @return 算法标识
	 */
	public String getAlgorithm() {
		return this.algorithm;
	}

	/**
	 * 获取密钥字节副本。
	 * @return 密钥字节副本
	 */
	public byte[] getEncoded() {
		return Arrays.copyOf(this.encoded, this.encoded.length);
	}

}
