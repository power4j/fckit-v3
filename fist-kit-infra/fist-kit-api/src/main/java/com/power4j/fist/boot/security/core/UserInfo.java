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

package com.power4j.fist.boot.security.core;

import com.power4j.fist.boot.common.utils.MapKit;
import lombok.Data;
import org.apache.commons.lang3.reflect.Typed;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * POJO of AuthUser so we don't have to import Spring Security
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/9/13
 * @since 1.0
 */
@Data
public class UserInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;

	private String username;

	@Nullable
	private String nickName;

	@Nullable
	private String avatarUrl;

	@Nullable
	private Map<String, Object> meta;

	/**
	 * 获取扩展属性,并进行类型转换
	 * @param <T> 类型
	 * @param key 属性的key
	 * @return 如果Key不存在则返回empty
	 * @see com.power4j.fist.boot.security.core.SecurityConstant.UserProp
	 */
	public <T> Optional<T> getMetaProp(String key, Class<T> clazz) {
		return getRawMetaProp(key).map(clazz::cast);
	}

	public <T> Optional<T> getMetaProp(String key, Typed<T> typed) {
		return MapKit.useValue(meta, key, typed);
	}

	/**
	 * 获取扩展属性
	 * @param key 属性的key
	 * @return 如果Key不存在则返回empty
	 * @see com.power4j.fist.boot.security.core.SecurityConstant.UserProp
	 */
	public Optional<Object> getRawMetaProp(String key) {
		if (Objects.isNull(meta) || !meta.containsKey(key)) {
			return Optional.empty();
		}
		return Optional.ofNullable(meta.get(key));
	}

	@Nullable
	public Object putMetaProp(String key, Object value) {
		return useMeta().put(key, value);
	}

	@Nullable
	public Object putMetaPropIfAbsent(String key, Object value) {
		return useMeta().putIfAbsent(key, value);
	}

	private Map<String, Object> useMeta() {
		if (Objects.isNull(meta)) {
			meta = new HashMap<>(8);
		}
		return meta;
	}

	public Map<String, Object> getMeta() {
		return useMeta();
	}

}
