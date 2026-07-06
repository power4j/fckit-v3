package com.power4j.fist.trace.context.runtime;

import com.power4j.fist.trace.context.item.TraceContextItem;
import java.util.List;

/**
 * 追踪上下文项注册表。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.15
 */
public interface TraceContextRegistry {

	List<TraceContextItem> items();

}
