package com.power4j.fist3.examples.trace.extension;

import com.power4j.fist.trace.context.TraceContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TraceController {

	private static final Logger log = LoggerFactory.getLogger(TraceController.class);

	@GetMapping("/trace")
	public Map<String, String> trace() {
		Map<String, String> values = TraceContexts.requireCurrent().asMap();
		log.info("trace context extension example");
		return values;
	}

}
