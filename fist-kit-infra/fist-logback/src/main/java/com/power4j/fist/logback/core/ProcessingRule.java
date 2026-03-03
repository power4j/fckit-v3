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

package com.power4j.fist.logback.core;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.api.Transformer;

/**
 * 处理规则：将 {@link Detector} 与 {@link Transformer} 绑定为一条命名规则。
 *
 * @author CJ (power4j@outlook.com)
 * @since 3.12
 */
public final class ProcessingRule {

	private final String name;

	private final Detector detector;

	private final Transformer transformer;

	private final int order;

	public ProcessingRule(String name, Detector detector, Transformer transformer, int order) {
		this.name = name;
		this.detector = detector;
		this.transformer = transformer;
		this.order = order;
	}

	public String getName() {
		return name;
	}

	public Detector getDetector() {
		return detector;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public int getOrder() {
		return order;
	}

}
