package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.Slf4jTraceMdcContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContexts;
import com.power4j.fist.trace.context.TraceScope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Method;

/**
 * {@link TraceSpan} AOP 支持。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@Aspect
public class TraceSpanAspect {

	private final TraceContextRuntime runtime;

	public TraceSpanAspect(TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Around("@annotation(com.power4j.fist.trace.boot.autoconfigure.TraceSpan)")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		TraceSpan annotation = resolveAnnotation(point);
		try (TraceScope ignored = TraceContexts.pushSpan(resolveSpanId(point, annotation));
				Slf4jTraceMdcContext mdc = new Slf4jTraceMdcContext()) {
			this.runtime.syncMdc(mdc);
			return point.proceed();
		}
	}

	private static TraceSpan resolveAnnotation(ProceedingJoinPoint point) {
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = AopUtils.getMostSpecificMethod(signature.getMethod(),
				AopUtils.getTargetClass(point.getTarget()));
		return method.getAnnotation(TraceSpan.class);
	}

	private static String resolveSpanId(ProceedingJoinPoint point, TraceSpan annotation) {
		String span = annotation.value();
		if (span.startsWith(".")) {
			return span.substring(1);
		}
		TraceSpanGroup group = AopUtils.getTargetClass(point.getTarget()).getAnnotation(TraceSpanGroup.class);
		if (group == null || group.value().isBlank()) {
			return span;
		}
		return group.value() + "." + span;
	}

}
