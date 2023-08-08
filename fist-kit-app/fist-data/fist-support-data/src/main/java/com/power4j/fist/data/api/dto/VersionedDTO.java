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

package com.power4j.fist.data.api.dto;

import com.power4j.fist.data.crud.validate.Groups;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

/**
 * 方便客户端回传版本号,否则需要服务端查询->合并->更新
 *
 * @author CJ (power4j@outlook.com)
 * @date 2021/10/27
 * @since 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VersionedDTO extends AuditDTO {

	@Schema(description = "数据版本")
	@NotNull(groups = { Groups.Update.class })
	private Integer version;

}
