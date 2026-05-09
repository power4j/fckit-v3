package com.power4j.fist.sde.core.json;

import java.lang.reflect.Type;

public interface SecureJsonCodec {

	byte[] serialize(Object value, Type valueType);

}
