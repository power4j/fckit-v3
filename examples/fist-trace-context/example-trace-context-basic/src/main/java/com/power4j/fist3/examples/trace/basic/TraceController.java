package com.power4j.fist3.examples.trace.basic;

import com.power4j.fist.trace.context.TraceContext;
import com.power4j.fist.trace.context.TraceContexts;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TraceController {

	private final TraceService traceService;

	public TraceController(TraceService traceService) {
		this.traceService = traceService;
	}

	@GetMapping("/trace")
	public Map<String, Object> trace() {
		this.traceService.work();
		TraceContext context = TraceContexts.requireCurrent();
		return Map.of("values", context.asMap(), "spanId", context.getSpanId().orElse(""));
	}

}
