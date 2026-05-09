package com.power4j.fist.sde.core.json;

import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;

public interface SecureJsonCodec {

	byte[] serialize(@Nullable Object value, @Nullable Type valueType);

}
