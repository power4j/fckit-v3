package com.power4j.fist3.examples.trace.basic;

import com.power4j.fist.trace.boot.autoconfigure.TraceSpan;
import com.power4j.fist.trace.boot.autoconfigure.TraceSpanGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@TraceSpanGroup("basic")
public class TraceService {

	private static final Logger log = LoggerFactory.getLogger(TraceService.class);

	@TraceSpan("work")
	public void work() {
		log.info("trace context basic example");
	}

}
