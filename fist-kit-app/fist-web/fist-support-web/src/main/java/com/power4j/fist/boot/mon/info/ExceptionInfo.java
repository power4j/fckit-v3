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

package com.power4j.fist.boot.mon.info;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.lang.Nullable;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/14
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionInfo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private String ex;

	@Nullable
	private String exMsg;

	private String exStack;

	public static ExceptionInfo from(Throwable e, int stacktraceLimit) {
		ExceptionInfo info = new ExceptionInfo();
		info.setEx(e.getClass().getName());
		info.setExMsg(e.getMessage());
		info.setExStack(StringUtils.truncate(ExceptionUtils.getStackTrace(e), stacktraceLimit));
		return info;
	}

	public static ExceptionInfo from(Throwable e) {
		return from(e, 5000);
	}

}
