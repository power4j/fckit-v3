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

package com.power4j.fist.boot.mybaits.handler.column;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.io.IOException;
import java.util.Map;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/12/28
 * @since 1.0
 */
@MappedTypes(value = { Map.class })
@MappedJdbcTypes(value = JdbcType.VARCHAR)
public class StringMapHandler extends JacksonTypeHandler {

	public StringMapHandler(Class<?> type) {
		super(type);
	}

	@Override
	public Object parse(String json) {
		try {
			return getObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
			});
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
