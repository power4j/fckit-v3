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

package com.power4j.fist.boot.mybaits.handler;

import com.power4j.fist.boot.mybaits.entity.AuditEntity;
import com.power4j.fist.boot.security.core.UserInfo;
import com.power4j.fist.boot.security.core.UserInfoSupplier;
import com.power4j.fist.data.constant.DataConstant;
import com.power4j.fist.mybatis.extension.meta.ValueHandler;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public class AuditEntityFiller implements ValueHandler {

	private final static long UID_FALLBACK = -1L;

	private final UserInfoSupplier userInfoSupplier;

	@Setter
	private Supplier<LocalDateTime> dateTimeSupplier = LocalDateTime::now;

	public AuditEntityFiller(UserInfoSupplier userInfoSupplier) {
		this.userInfoSupplier = userInfoSupplier;
	}

	@Override
	public Object getValue(Object root, String fieldName, Class<?> fieldType) {
		if (fieldName.equals(AuditEntity.FIELD_CREATE_AT) && fieldType.equals(LocalDateTime.class)) {
			return getAuditTime();
		}
		if (fieldName.equals(AuditEntity.FIELD_UPDATE_AT) && fieldType.equals(LocalDateTime.class)) {
			return getAuditTime();
		}
		if (fieldName.equals(AuditEntity.FIELD_CREATE_BY) && fieldType.equals(Long.class)) {
			return getAuditUserId();
		}
		if (fieldName.equals(AuditEntity.FIELD_UPDATE_BY) && fieldType.equals(Long.class)) {
			return getAuditUserId();
		}
		if (fieldName.equals(AuditEntity.FIELD_LOW_ATTR) && fieldType.equals(Integer.class)) {
			return getLowAttrValue();
		}
		return null;
	}

	protected LocalDateTime getAuditTime() {
		return dateTimeSupplier.get();
	}

	protected Long getAuditUserId() {
		return Optional.ofNullable(userInfoSupplier.get()).map(UserInfo::getUserId).orElse(UID_FALLBACK);
	}

	protected Integer getLowAttrValue() {
		return UID_FALLBACK == getAuditUserId() ? DataConstant.LOW_ATTR_VALUE_SYSTEM : DataConstant.LOW_ATTR_VALUE_USER;
	}

}
