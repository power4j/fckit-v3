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

package com.power4j.fist.boot.web.reactive.log;

import com.power4j.fist.boot.web.reactive.constant.ContextConstant;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Operators;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/6/21
 * @since 1.0
 */
@Configuration
public class MdcContextLifterConfiguration {

	@PostConstruct
	private void contextOperatorHook() {
		Hooks.onEachOperator(ContextConstant.KEY_MDC,
				Operators.lift((s, coreSubscriber) -> new MdcContextLifter<>(coreSubscriber)));
	}

	@PreDestroy
	private void cleanupHook() {
		Hooks.resetOnEachOperator(ContextConstant.KEY_MDC);
	}

}
