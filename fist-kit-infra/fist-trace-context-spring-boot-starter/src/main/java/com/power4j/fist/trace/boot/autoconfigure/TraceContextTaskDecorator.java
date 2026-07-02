package com.power4j.fist.trace.boot.autoconfigure;

import com.power4j.fist.trace.context.Slf4jTraceMdcContext;
import com.power4j.fist.trace.context.TraceContextRuntime;
import com.power4j.fist.trace.context.TraceContextSnapshot;
import com.power4j.fist.trace.context.TraceContexts;
import com.power4j.fist.trace.context.TraceScope;
import org.springframework.core.task.TaskDecorator;

/**
 * 追踪上下文异步任务装饰器。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public class TraceContextTaskDecorator implements TaskDecorator {

	private final TraceContextRuntime runtime;

	public TraceContextTaskDecorator(TraceContextRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public Runnable decorate(Runnable runnable) {
		return TraceContexts.current().map(context -> decorate(runnable, context.snapshot())).orElse(runnable);
	}

	private Runnable decorate(Runnable runnable, TraceContextSnapshot snapshot) {
		return () -> {
			try (TraceScope ignored = this.runtime.restore(snapshot);
					Slf4jTraceMdcContext mdc = new Slf4jTraceMdcContext()) {
				this.runtime.syncMdc(mdc);
				runnable.run();
			}
		};
	}

}
