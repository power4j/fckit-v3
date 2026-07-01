package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 系统代码提供者。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface SystemCodeProvider {

	Optional<String> getSystemCode();

}
