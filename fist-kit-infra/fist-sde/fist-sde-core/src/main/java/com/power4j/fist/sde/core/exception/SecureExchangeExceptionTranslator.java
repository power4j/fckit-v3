package com.power4j.fist.sde.core.exception;

import com.power4j.fist.sde.core.SecureExchangeContext;
import org.jspecify.annotations.Nullable;

public interface SecureExchangeExceptionTranslator {

	@Nullable RuntimeException translate(SecureExchangeException exception, SecureExchangeContext context);

}
