package com.power4j.fist.trace.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 追踪上下文配置。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
@ConfigurationProperties(prefix = TraceContextProperties.PREFIX)
public class TraceContextProperties {

	public static final String PREFIX = "fist.trace-context";

	private boolean enabled;

	private Map<String, Item> items = new LinkedHashMap<>();

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Map<String, Item> getItems() {
		return this.items;
	}

	public void setItems(Map<String, Item> items) {
		this.items = items;
	}

	public static class Item {

		private boolean enabled = true;

		private String processor;

		private int order;

		private Map<String, String> props = new LinkedHashMap<>();

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getProcessor() {
			return this.processor;
		}

		public void setProcessor(String processor) {
			this.processor = processor;
		}

		public int getOrder() {
			return this.order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public Map<String, String> getProps() {
			return this.props;
		}

		public void setProps(Map<String, String> props) {
			this.props = props;
		}

	}

}
