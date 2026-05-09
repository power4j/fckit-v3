package com.power4j.fist.sde.core.exception;

import com.power4j.fist.sde.core.SecureExchangeContext;

public interface SecureExchangeExceptionTranslator {

	RuntimeException translate(SecureExchangeException exception, SecureExchangeContext context);

}
