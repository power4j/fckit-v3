package com.power4j.fist.trace.boot.autoconfigure.task;

import com.power4j.fist.trace.context.TraceContextRuntime;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.task.TaskDecorator;

/**
 * 将追踪上下文装饰能力合并到用户自定义 {@link TaskDecorator}。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextTaskDecoratorBeanPostProcessor implements BeanPostProcessor {

	private final ObjectProvider<TraceContextRuntime> runtime;

	public TraceContextTaskDecoratorBeanPostProcessor(ObjectProvider<TraceContextRuntime> runtime) {
		this.runtime = runtime;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof TaskDecorator taskDecorator && !(bean instanceof TraceContextTaskDecorator)) {
			return new CompositeTraceContextTaskDecorator(new TraceContextTaskDecorator(this.runtime.getObject()),
					taskDecorator);
		}
		return bean;
	}

	private record CompositeTraceContextTaskDecorator(TaskDecorator traceDecorator,
			TaskDecorator userDecorator) implements TaskDecorator {

		@Override
		public Runnable decorate(Runnable runnable) {
			return this.traceDecorator.decorate(this.userDecorator.decorate(runnable));
		}

	}

}
