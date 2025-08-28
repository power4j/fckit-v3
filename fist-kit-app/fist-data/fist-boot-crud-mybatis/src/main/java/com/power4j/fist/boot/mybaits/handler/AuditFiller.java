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

package com.power4j.fist.boot.mybaits.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.power4j.fist.data.constant.DataConstant;
import com.power4j.fist.boot.mybaits.entity.AuditEntity;
import com.power4j.fist.boot.security.core.UserInfo;
import com.power4j.fist.boot.security.core.UserInfoSupplier;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2021/7/2
 * @since 1.0
 * @deprecated use {@link AuditInfoSupplier}
 */
public class AuditFiller implements MetaObjectHandler {

	private final static long UID_FALLBACK = -1L;

	private final UserInfoSupplier userInfoSupplier;

	private Supplier<LocalDateTime> dateTimeSupplier = LocalDateTime::now;

	public AuditFiller(UserInfoSupplier userInfoSupplier) {
		this.userInfoSupplier = userInfoSupplier;
	}

	public void setDateTimeSupplier(Supplier<LocalDateTime> dateTimeSupplier) {
		this.dateTimeSupplier = dateTimeSupplier;
	}

	@Override
	public void insertFill(MetaObject metaObject) {
		Long uid = getAuditUserId();
		strictInsertFill(metaObject, AuditEntity.FIELD_CREATE_BY, () -> uid, Long.class);
		strictUpdateFill(metaObject, AuditEntity.FIELD_UPDATE_BY, () -> uid, Long.class);
		final LocalDateTime now = getAuditTime();
		strictInsertFill(metaObject, AuditEntity.FIELD_CREATE_AT, () -> now, LocalDateTime.class);
		strictUpdateFill(metaObject, AuditEntity.FIELD_UPDATE_AT, () -> now, LocalDateTime.class);
		strictInsertFill(metaObject, AuditEntity.FIELD_LOW_ATTR, this::getDefaultLowAttrValue, Integer.class);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		strictUpdateFill(metaObject, AuditEntity.FIELD_UPDATE_BY, this::getAuditUserId, Long.class);
		strictUpdateFill(metaObject, AuditEntity.FIELD_UPDATE_AT, this::getAuditTime, LocalDateTime.class);
	}

	protected LocalDateTime getAuditTime() {
		return dateTimeSupplier.get();
	}

	protected Long getAuditUserId() {
		return Optional.ofNullable(userInfoSupplier.get()).map(UserInfo::getUserId).orElse(UID_FALLBACK);
	}

	protected Integer getDefaultLowAttrValue() {
		return UID_FALLBACK == getAuditUserId() ? DataConstant.LOW_ATTR_VALUE_SYSTEM : DataConstant.LOW_ATTR_VALUE_USER;
	}

}
