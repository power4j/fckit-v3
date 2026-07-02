package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 系统代码提供者。
 * <p>
 * 默认 {@code system-code} 处理器在启动期读取该接口并冻结返回值，不支持运行期动态变化。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface SystemCodeProvider {

	Optional<String> getSystemCode();

}
