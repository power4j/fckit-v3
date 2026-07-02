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

package com.power4j.fist.cloud.autoconfigure.rpc.feign;

import com.power4j.fist.cloud.rpc.feign.RelayHandler;
import com.power4j.fist.cloud.rpc.feign.RelayInterceptor;
import com.power4j.fist.cloud.rpc.feign.TraceRelayHandler;
import com.power4j.fist.cloud.rpc.feign.UserRelayHandler;
import com.power4j.fist.trace.context.TraceContextRuntime;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/8/11
 * @since 1.0
 */
@AutoConfiguration
@AutoConfigureAfter(name = "com.power4j.fist.trace.boot.autoconfigure.TraceContextAutoConfiguration")
@ComponentScan(basePackages = { "com.power4j.fist.cloud.autoconfigure.rpc.feign.error" })
public class FeignClientAutoConfiguration {

	@Bean
	public RequestInterceptor requestInterceptor(List<RelayHandler> handlers) {
		return new RelayInterceptor(handlers);
	}

	@Bean
	@ConditionalOnBean(TraceContextRuntime.class)
	public TraceRelayHandler traceRelayHandler(TraceContextRuntime runtime) {
		return new TraceRelayHandler(runtime);
	}

	@Bean
	public UserRelayHandler userRelayHandler() {
		return new UserRelayHandler();
	}

	// TODO: 统一降级处理

}
