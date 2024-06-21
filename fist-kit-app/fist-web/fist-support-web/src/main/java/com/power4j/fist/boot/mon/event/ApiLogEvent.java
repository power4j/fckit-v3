/*
 * Copyright 2021 ChenJun (power4j@outlook.com & https://github.com/John-Chan)
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

package com.power4j.fist.boot.mon.event;

import com.power4j.fist.boot.mon.info.ApiResponseInfo;
import com.power4j.fist.boot.mon.info.AuthInfo;
import com.power4j.fist.boot.mon.info.ExceptionInfo;
import com.power4j.fist.boot.mon.info.HttpRequestInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/3/31
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogEvent implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String appName;

	private String operation;

	private HttpRequestInfo requestInfo;

	private ApiResponseInfo responseInfo;

	private AuthInfo authInfo;

	@Nullable
	private ExceptionInfo error;

	/**
	 * UTC
	 */
	private LocalDateTime time;

	private Duration duration;

}
