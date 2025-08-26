/*
 * Copyright 2025. ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.mybatis.extension.meta;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public class MetaHandlerRegistry implements MetaHandlerResolver {

	private final Map<Class<? extends MetaHandler>, MetaHandler> registry = new ConcurrentHashMap<>();

	/**
	 * 注册,如果已经注册,则覆盖
	 * @param cls 注册的类型
	 * @param signer 注册的实例
	 * @param <T> 类型
	 */
	public <T extends MetaHandler> void register(Class<T> cls, T signer) {
		registry.put(cls, signer);
	}

	/**
	 * 注册,如果已经注册,则忽略
	 * @param cls 注册的类型
	 * @param signer 注册的实例
	 * @param <T> 类型
	 */
	public <T extends MetaHandler> void registerIfAbsent(Class<T> cls, T signer) {
		registry.putIfAbsent(cls, signer);
	}

	/**
	 * 更新注册表
	 * @param consumer 更新函数
	 */
	public void updateRegistry(Consumer<Map<Class<? extends MetaHandler>, MetaHandler>> consumer) {
		consumer.accept(registry);
	}

	/**
	 * 内部Map的拷贝
	 * @return Map
	 */
	public Map<Class<? extends MetaHandler>, MetaHandler> copiedMap() {
		return new HashMap<>(registry);
	}

	@Override
	public <T extends MetaHandler> Optional<T> resolve(Class<T> cls) {
		return Optional.ofNullable((T) registry.get(cls));
	}

}
