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

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;
import com.power4j.fist.mybatis.extension.meta.annotation.FillWith;
import lombok.Getter;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public class MetaHandlerCompose implements MetaObjectHandler {

	private final ValueHandlerResolver resolver;

	@Nullable
	private final ValueHandler globalHandler;

	public MetaHandlerCompose(ValueHandlerResolver resolver, @Nullable ValueHandler globalHandler) {
		this.resolver = resolver;
		this.globalHandler = globalHandler;
	}

	public MetaHandlerCompose(ValueHandlerResolver resolver) {
		this(resolver, null);
	}

	@Override
	public void insertFill(MetaObject metaObject) {
		Object originalObject = metaObject.getOriginalObject();
		List<FillTarget> fillTargets = getFillTargets(originalObject);
		for (FillTarget fillTarget : fillTargets) {
			fillInsertValue(metaObject, fillTarget);
		}
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		Object originalObject = metaObject.getOriginalObject();
		List<FillTarget> fillTargets = getFillTargets(originalObject);
		for (FillTarget fillTarget : fillTargets) {
			fillUpdateValue(metaObject, fillTarget);
		}
	}

	protected List<FillTarget> getFillTargets(Object originalObject) {
		Class<?> clazz = originalObject.getClass();
		List<FillTarget> annotations = new ArrayList<>(4);
		for (Field field : clazz.getDeclaredFields()) {

			if (field.isAnnotationPresent(FillWith.class)) {
				FillWith fillWith = field.getAnnotation(FillWith.class);
				annotations.add(FillTarget.of(fillWith, field));
			}
			else {
				// use TableField alone
				if (field.isAnnotationPresent(TableField.class)) {
					TableField tableField = field.getAnnotation(TableField.class);
					// still try fill with global handler
					if (tableField.fill() != FieldFill.DEFAULT) {
						annotations.add(FillTarget.of(null, field));
					}
				}
			}
		}
		return annotations.stream()
			.sorted(Comparator.comparingInt(
					o -> Optional.ofNullable(o.getFillWith()).map(FillWith::order).orElse(FillWith.LOWEST_ORDER)))
			.collect(Collectors.toList());
	}

	protected Object getFieldValue(Object originalObject, FillTarget fillTarget) {
		try {
			ValueHandler handler;
			FillWith fillWith = fillTarget.getFillWith();
			if (fillWith == null) {
				handler = globalHandler;
			}
			else {
				Class<? extends ValueHandler> handlerClass = fillWith.handler();
				handler = resolver.resolve(handlerClass).orElse(globalHandler);
			}
			if (handler == null) {
				throw new MetaHandlerException(
						"could not resolve value handler for: " + fillTarget.getField().getName());
			}
			return handler.getValue(originalObject, fillTarget.field.getName(), fillTarget.field.getType());
		}
		catch (Exception e) {
			throw new MetaHandlerException(e);
		}
	}

	protected void fillInsertValue(MetaObject metaObject, FillTarget fillTarget) {
		strictFillValue(true, findTableInfo(metaObject), metaObject, fillTarget,
				() -> getFieldValue(metaObject.getOriginalObject(), fillTarget));
	}

	protected void fillUpdateValue(MetaObject metaObject, FillTarget fillTarget) {
		strictFillValue(false, findTableInfo(metaObject), metaObject, fillTarget,
				() -> getFieldValue(metaObject.getOriginalObject(), fillTarget));
	}

	protected void strictFillValue(boolean insertFill, TableInfo tableInfo, MetaObject metaObject,
			FillTarget fillTarget, Supplier<?> valueSupplier) {
		if ((insertFill && tableInfo.isWithInsertFill()) || (!insertFill && tableInfo.isWithUpdateFill())) {
			final String fieldName = fillTarget.getField().getName();
			final Class<?> fieldType = fillTarget.getField().getType();
			for (TableFieldInfo fieldInfo : tableInfo.getFieldList()) {
				final String property = fieldInfo.getProperty();
				final Class<?> propertyType = fieldInfo.getPropertyType();
				if (fieldName.equals(property) && fieldType.equals(propertyType)
						&& ((insertFill && fieldInfo.isWithInsertFill())
								|| (!insertFill && fieldInfo.isWithUpdateFill()))) {
					strictFillStrategy(metaObject, fieldName, valueSupplier);
					break;
				}
			}
		}
	}

	@Getter
	protected static class FillTarget {

		/**
		 * Null means use FieldFill alone
		 */
		@Nullable
		private final FillWith fillWith;

		private final Field field;

		FillTarget(@Nullable FillWith fillWith, Field field) {
			this.fillWith = fillWith;
			this.field = field;
		}

		static FillTarget of(@Nullable FillWith annotation, Field field) {
			return new FillTarget(annotation, field);
		}

	}

}
