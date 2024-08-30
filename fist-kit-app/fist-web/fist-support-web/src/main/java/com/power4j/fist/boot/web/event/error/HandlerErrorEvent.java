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

package com.power4j.fist.boot.web.event.error;

import com.power4j.fist.boot.mon.info.EnvInfo;
import com.power4j.fist.boot.mon.info.ExceptionInfo;
import com.power4j.fist.boot.mon.info.TraceInfo;
import lombok.Data;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/14
 * @since 1.0
 */
@Data
public class HandlerErrorEvent implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String appName;

	/**
	 * UTC
	 */
	private LocalDateTime time;

	private TraceInfo traceInfo;

	private EnvInfo envInfo;

	private RequestInfo requestInfo;

	@Nullable
	private ExceptionInfo error;

	private Map<String, Object> extraInfo = new HashMap<>();

}
