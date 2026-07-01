package com.power4j.fist.trace.context;

import java.util.Optional;

/**
 * 单值追踪上下文项基类。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public abstract class AbstractSingleValueTraceContextItem extends AbstractTraceContextItem {

	private final String contextName;

	private final Optional<String> inboundHeader;

	private final Optional<String> outboundHeader;

	private final Optional<String> mdcName;

	protected AbstractSingleValueTraceContextItem(String processor, String contextName, Optional<String> inboundHeader,
			Optional<String> outboundHeader, Optional<String> mdcName) {
		super(processor);
		this.contextName = contextName;
		this.inboundHeader = inboundHeader;
		this.outboundHeader = outboundHeader;
		this.mdcName = mdcName;
	}

	public String contextName() {
		return this.contextName;
	}

	public Optional<String> inboundHeader() {
		return this.inboundHeader;
	}

	public Optional<String> outboundHeader() {
		return this.outboundHeader;
	}

	public Optional<String> mdcName() {
		return this.mdcName;
	}

	@Override
	public void onInbound(InboundTraceContext context) {
		Optional<String> value = this.inboundHeader.flatMap(context::readHeader).or(() -> generateValue(context));
		value.ifPresent(text -> context.putValue(this.contextName, text));
	}

	@Override
	public void onOutbound(OutboundTraceContext context) {
		this.outboundHeader.ifPresent(header -> context.getValue(this.contextName)
			.ifPresent(value -> context.writeHeader(header, value)));
	}

	@Override
	public void onMdc(TraceMdcContext context) {
		this.mdcName.ifPresent(name -> context.getValue(this.contextName).ifPresent(value -> context.putMdc(name, value)));
	}

	protected Optional<String> generateValue(InboundTraceContext context) {
		return Optional.empty();
	}

}
