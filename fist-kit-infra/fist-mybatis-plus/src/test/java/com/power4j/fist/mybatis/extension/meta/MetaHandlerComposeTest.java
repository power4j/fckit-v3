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
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;
import com.power4j.fist.mybatis.extension.meta.annotation.FillWith;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
class MetaHandlerComposeTest {

	private static final String MOCK_META_VALUE1 = "fill_1";

	@Mock
	private ValueHandlerRegistry handlerRegistry;

	@Mock
	private MetaObject metaObject;

	@Mock
	private FakeHandler fakeHandler;

	@Mock
	private CountHandler countHandler;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(fakeHandler.getValue(any(), any(), any())).thenReturn(MOCK_META_VALUE1);
		when(handlerRegistry.resolve(any())).thenReturn(Optional.of(fakeHandler));
	}

	@Test
	void shouldThrowExceptionWhenValueHandlerNotFound() {
		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, Foo.class);

		when(handlerRegistry.resolve(any())).thenReturn(Optional.empty());

		MetaHandlerCompose handlerCompose = new MetaHandlerCompose(handlerRegistry);

		Foo foo = new Foo();
		when(metaObject.getOriginalObject()).thenReturn(foo);
		Assertions.assertThrows(MetaHandlerException.class, () -> handlerCompose.insertFill(metaObject));
		Assertions.assertThrows(MetaHandlerException.class, () -> handlerCompose.updateFill(metaObject));

	}

	@Test
	void shouldUseGlobalHandler() {

		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, MoMo.class);

		when(handlerRegistry.resolve(any())).thenReturn(Optional.empty());

		MetaHandlerCompose handler = new MetaHandlerCompose(handlerRegistry, fakeHandler);

		MoMo entity = new MoMo();
		when(metaObject.getOriginalObject()).thenReturn(entity);

		// Act
		handler.insertFill(metaObject);

		verify(fakeHandler, times(1)).getValue(any(), eq("meta"), any());

	}

	@Test
	void shouldCallHandlerByInsertFill() {
		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, Bar.class);

		when(handlerRegistry.resolve(eq(CountHandler.class))).thenReturn(Optional.of(countHandler));

		MetaHandlerCompose handler = new MetaHandlerCompose(handlerRegistry);

		Bar entity = new Bar();
		when(metaObject.getOriginalObject()).thenReturn(entity);

		// Act
		handler.insertFill(metaObject);

		verify(metaObject, never()).setValue(any(), eq("updateMeta"));
		verify(countHandler, times(1)).getValue(any(), eq("insertMeta"), any());
		verify(countHandler, times(1)).getValue(any(), eq("allMeta"), any());

	}

	@Test
	void shouldCallHandlerByUpdateFill() {
		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, Bar.class);

		when(handlerRegistry.resolve(eq(CountHandler.class))).thenReturn(Optional.of(countHandler));

		MetaHandlerCompose handler = new MetaHandlerCompose(handlerRegistry);

		Bar entity = new Bar();
		when(metaObject.getOriginalObject()).thenReturn(entity);

		// Act
		handler.updateFill(metaObject);

		verify(metaObject, never()).setValue(any(), eq("insertMeta"));
		verify(countHandler, times(1)).getValue(any(), eq("updateMeta"), any());
		verify(countHandler, times(1)).getValue(any(), eq("allMeta"), any());

	}

	@Test
	void shouldInsertFillWithOrder() {
		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, Bar.class);

		CountHandler countHandler = new CountHandler(1);
		when(handlerRegistry.resolve(eq(CountHandler.class))).thenReturn(Optional.of(countHandler));

		MetaHandlerCompose handler = new MetaHandlerCompose(handlerRegistry);

		Bar entity = new Bar();
		when(metaObject.getOriginalObject()).thenReturn(entity);

		// Act
		handler.insertFill(metaObject);

		verify(metaObject, times(1)).setValue(eq("allMeta"), eq("1"));
		verify(metaObject, times(1)).setValue(eq("insertMeta"), eq("2"));
	}

	@Test
	void shouldUpdateFillWithOrder() {
		MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new Configuration(), null);
		TableInfoHelper.initTableInfo(mapperBuilderAssistant, Bar.class);

		CountHandler countHandler = new CountHandler(1);
		when(handlerRegistry.resolve(eq(CountHandler.class))).thenReturn(Optional.of(countHandler));

		MetaHandlerCompose handler = new MetaHandlerCompose(handlerRegistry);

		Bar entity = new Bar();
		when(metaObject.getOriginalObject()).thenReturn(entity);

		// Act
		handler.updateFill(metaObject);

		verify(metaObject, times(1)).setValue(eq("allMeta"), eq("1"));
		verify(metaObject, times(1)).setValue(eq("updateMeta"), eq("2"));
	}

	@Data
	public static class Foo {

		private String name;

		@FillWith(handler = FakeHandler.class)
		@TableField(fill = FieldFill.INSERT_UPDATE)
		private String meta;

	}

	@Data
	public static class Base {

		@FillWith(handler = CountHandler.class)
		@TableField(value = "all_meta_value", fill = FieldFill.INSERT_UPDATE)
		private String allMeta;

	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	public static class Bar extends Base {

		@FillWith(handler = CountHandler.class, order = FillWith.LOWEST_ORDER)
		@TableField(fill = FieldFill.INSERT)
		private String insertMeta;

		@FillWith(handler = CountHandler.class, order = FillWith.LOWEST_ORDER)
		@TableField(fill = FieldFill.UPDATE)
		private String updateMeta;

	}

	@Data
	public static class MoMo {

		private String name;

		@TableField(fill = FieldFill.INSERT_UPDATE)
		private String meta;

	}

	public static class FakeHandler implements ValueHandler {

		@Override
		public Object getValue(Object root, String fieldName, Class<?> fieldType) {
			return MOCK_META_VALUE1;
		}

	}

	public static class CountHandler implements ValueHandler {

		private final AtomicInteger count;

		public CountHandler(int initValue) {
			this.count = new AtomicInteger(initValue);
		}

		@Override
		public Object getValue(Object root, String fieldName, Class<?> fieldType) {
			return String.valueOf(count.getAndIncrement());
		}

		public int getCount() {
			return count.get();
		}

	}

}
