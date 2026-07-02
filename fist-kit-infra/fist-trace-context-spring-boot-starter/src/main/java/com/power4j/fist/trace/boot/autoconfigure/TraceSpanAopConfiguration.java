package com.power4j.fist.trace.boot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * span 注解 AOP 配置。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = TraceContextProperties.PREFIX + ".span", name = "enabled", havingValue = "true",
		matchIfMissing = true)
@EnableAspectJAutoProxy
class TraceSpanAopConfiguration {

}
