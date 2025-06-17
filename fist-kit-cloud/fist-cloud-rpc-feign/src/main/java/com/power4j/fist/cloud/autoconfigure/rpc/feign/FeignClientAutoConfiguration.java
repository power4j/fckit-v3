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

import com.power4j.fist.boot.web.constant.HttpConstant;
import com.power4j.fist.cloud.rpc.feign.HeaderRelayHandler;
import com.power4j.fist.cloud.rpc.feign.RelayInterceptor;
import com.power4j.fist.cloud.rpc.feign.UserRelayHandler;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;
import java.util.Set;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/8/11
 * @since 1.0
 */
@AutoConfiguration
@ComponentScan(basePackages = { "com.power4j.fist.cloud.autoconfigure.rpc.feign.error" })
public class FeignClientAutoConfiguration {

	@Bean
	public RequestInterceptor requestInterceptor() {
		UserRelayHandler userRelayHandler = new UserRelayHandler();
		HeaderRelayHandler headerRelayHandler = new HeaderRelayHandler(Set.of(HttpConstant.Header.KEY_REQUEST_ID));
		return new RelayInterceptor(List.of(headerRelayHandler, userRelayHandler));
	}

	// TODO: 统一降级处理

}
