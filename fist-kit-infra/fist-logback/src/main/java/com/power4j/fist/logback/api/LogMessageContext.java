/*
 * Copyright 2026. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.fist.logback.api;

import ch.qos.logback.classic.Level;
import org.slf4j.Marker;

import java.util.Map;

/**
 * 日志消息处理上下文，携带当前日志事件的元数据。
 * <p>
 * MDC 快照采用懒加载策略：仅在 {@link #getMdc()} 被显式调用时才执行快照。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public class LogMessageContext {

	private final Level level;

	private final String loggerName;

	private final String threadName;

	private final long timestamp;

	private final Marker marker;

	private final MdcSupplier mdcSupplier;

	private volatile Map<String, String> mdc;

	/**
	 * MDC 快照提供者，用于延迟获取 MDC 副本。
	 */
	@FunctionalInterface
	public interface MdcSupplier {

		Map<String, String> get();

	}

	public LogMessageContext(Level level, String loggerName, String threadName, long timestamp, Marker marker,
			MdcSupplier mdcSupplier) {
		this.level = level;
		this.loggerName = loggerName;
		this.threadName = threadName;
		this.timestamp = timestamp;
		this.marker = marker;
		this.mdcSupplier = mdcSupplier;
	}

	public Level getLevel() {
		return level;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public String getThreadName() {
		return threadName;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Marker getMarker() {
		return marker;
	}

	/**
	 * 获取 MDC 快照（懒加载）。
	 * @return MDC 键值对，可能为 null
	 */
	public Map<String, String> getMdc() {
		if (mdc == null) {
			synchronized (this) {
				if (mdc == null) {
					mdc = mdcSupplier.get();
				}
			}
		}
		return mdc;
	}

}
