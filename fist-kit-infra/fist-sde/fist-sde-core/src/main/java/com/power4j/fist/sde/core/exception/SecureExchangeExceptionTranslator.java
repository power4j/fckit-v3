package com.power4j.fist.sde.core.exception;

import com.power4j.fist.sde.core.SecureExchangeContext;
import org.jspecify.annotations.Nullable;

/**
 * SDE 异常转换契约。
 * <p>
 * 服务端可通过实现该接口将内部 SDE 异常转换为应用自己的运行时异常或错误响应模型。
 */
public interface SecureExchangeExceptionTranslator {

	/**
	 * 转换 SDE 异常。
	 * @param exception 原始 SDE 异常
	 * @param context 交换上下文
	 * @return 转换后的运行时异常；返回 {@code null} 表示继续使用原异常
	 */
	@Nullable RuntimeException translate(SecureExchangeException exception, SecureExchangeContext context);

}
