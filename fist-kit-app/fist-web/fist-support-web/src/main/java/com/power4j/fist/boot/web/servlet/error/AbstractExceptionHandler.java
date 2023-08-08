/*
 *  Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
 *
 *  Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  <p>
 *  http://www.gnu.org/licenses/lgpl.html
 *  <p>
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.power4j.fist.boot.web.servlet.error;

import com.power4j.coca.kit.common.datetime.DateTimeKit;
import com.power4j.fist.boot.mon.info.ExceptionInfo;
import com.power4j.fist.boot.mon.info.InfoUtil;
import com.power4j.fist.boot.mon.info.TraceInfo;
import com.power4j.fist.boot.mon.info.TraceInfoResolver;
import com.power4j.fist.support.spring.util.ApplicationContextHolder;
import com.power4j.fist.support.spring.util.SpringEventUtil;
import com.power4j.fist.boot.web.event.error.HandlerErrorEvent;
import com.power4j.fist.boot.web.event.error.RequestInfo;
import com.power4j.fist.support.spring.web.servlet.util.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/14
 * @since 1.0
 */
public class AbstractExceptionHandler {

	private TraceInfoResolver<HttpServletRequest> traceInfoResolver = request -> Optional.empty();

	@Autowired(required = false)
	public void setTraceInfoResolver(TraceInfoResolver<HttpServletRequest> traceInfoResolver) {
		this.traceInfoResolver = traceInfoResolver;
	}

	/**
	 * 发送异常报警
	 * @param e 异常
	 */
	protected void doNotify(Throwable e) {
		HandlerErrorEvent event = createErrorEvent(e);
		if (filterErrorEvent(event)) {
			SpringEventUtil.publishEvent(event);
		}
	}

	protected HandlerErrorEvent createErrorEvent(Throwable e) {
		String appName = ApplicationContextHolder.getContextOptional()
			.map(ApplicationContext::getApplicationName)
			.orElse("未知应用");
		TraceInfo traceInfo = HttpServletRequestUtil.getCurrentRequestIfAvailable()
			.flatMap(o -> traceInfoResolver.resolve(o))
			.orElse(new TraceInfo());
		HandlerErrorEvent handlerErrorEvent = new HandlerErrorEvent();
		handlerErrorEvent.setAppName(appName);
		handlerErrorEvent.setTime(DateTimeKit.utcNow());
		handlerErrorEvent.setEnvInfo(InfoUtil.getEnvInfo());
		handlerErrorEvent.setError(ExceptionInfo.from(e));
		handlerErrorEvent.setRequestInfo(RequestInfo.from(HttpServletRequestUtil.getCurrentRequest()));
		handlerErrorEvent.setTraceInfo(traceInfo);

		return handlerErrorEvent;
	}

	/**
	 * 过滤ErrorEvent
	 * @param handlerErrorEvent 原始信息
	 * @return 返回 false 表示终止发送事件
	 */
	protected boolean filterErrorEvent(HandlerErrorEvent handlerErrorEvent) {
		return true;
	}

}
