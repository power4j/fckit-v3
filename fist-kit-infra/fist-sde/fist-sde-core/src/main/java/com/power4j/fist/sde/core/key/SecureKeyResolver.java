package com.power4j.fist.sde.core.key;

import com.power4j.fist.sde.core.SecureKey;

public interface SecureKeyResolver {

	SecureKey resolve(SecureKeyContext context);

}
