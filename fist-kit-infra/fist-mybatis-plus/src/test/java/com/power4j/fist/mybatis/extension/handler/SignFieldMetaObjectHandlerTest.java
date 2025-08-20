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

package com.power4j.fist.mybatis.extension.handler;

import com.power4j.fist.mybatis.extension.api.sign.DefaultRegistry;
import com.power4j.fist.mybatis.extension.api.sign.SignField;
import com.power4j.fist.mybatis.extension.api.sign.Signer;
import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;
import lombok.Data;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
class SignFieldMetaObjectHandlerTest {

	private static final String MOCK_SIGN_VALUE = "mocked_sign_value";

	@InjectMocks
	private SignFieldMetaObjectHandler handler;

	@Mock
	private DefaultRegistry signerRegistry;

	@Mock
	private MetaObject metaObject;

	@Mock
	private Signer mockSigner;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		when(mockSigner.apply(any())).thenReturn(MOCK_SIGN_VALUE);
		when(signerRegistry.resolve(any())).thenReturn(Optional.of(mockSigner));
	}

	@Test
	void shouldResolveSignerOnInsert() {
		// Insert case
		InsertEntity insertEntity = new InsertEntity();
		when(metaObject.getOriginalObject()).thenReturn(insertEntity);

		handler.insertFill(metaObject);
		verify(signerRegistry, times(1)).resolve(MockSigner.class);

	}

	@Test
	void shouldResolveSignerOnUpdate() {
		// Update case
		UpdateEntity updateEntity = new UpdateEntity();
		when(metaObject.getOriginalObject()).thenReturn(updateEntity);

		handler.updateFill(metaObject);
		verify(signerRegistry, times(1)).resolve(MockSigner.class);
	}

	@Test
	void shouldNotResolveSignerOnInsert() {
		// Insert case
		UpdateEntity updateEntity = new UpdateEntity();
		when(metaObject.getOriginalObject()).thenReturn(updateEntity);

		handler.insertFill(metaObject);
		verify(signerRegistry, never()).resolve(MockSigner.class);

	}

	@Test
	void shouldNotResolveSignerOnUpdate() {
		// Update case
		InsertEntity insertEntity = new InsertEntity();
		when(metaObject.getOriginalObject()).thenReturn(insertEntity);

		handler.updateFill(metaObject);
		verify(signerRegistry, never()).resolve(MockSigner.class);
	}

	@Test
	void shouldNotResolveSignerOnInsertUpdate() {
		// Update case
		EmptyEntity emptyEntity = new EmptyEntity();
		when(metaObject.getOriginalObject()).thenReturn(emptyEntity);

		handler.insertFill(metaObject);
		verify(signerRegistry, never()).resolve(MockSigner.class);
		handler.updateFill(metaObject);
		verify(signerRegistry, never()).resolve(MockSigner.class);
	}

	@Test
	void shouldThrowExceptionOnResolveFail() {
		when(signerRegistry.resolve(any())).thenReturn(Optional.empty());

		InsertEntity insertEntity = new InsertEntity();
		when(metaObject.getOriginalObject()).thenReturn(insertEntity);
		Assertions.assertThrows(MetaHandlerException.class, () -> handler.insertFill(metaObject));

		UpdateEntity updateEntity = new UpdateEntity();
		when(metaObject.getOriginalObject()).thenReturn(updateEntity);
		Assertions.assertThrows(MetaHandlerException.class, () -> handler.updateFill(metaObject));
	}

	@Test
	void shouldFillOnInsert() {
		// Insert case
		InsertEntity insertEntity = new InsertEntity();
		when(metaObject.getOriginalObject()).thenReturn(insertEntity);

		handler.insertFill(metaObject);
		verify(metaObject).setValue(eq("signValue"), eq(MOCK_SIGN_VALUE));
	}

	@Test
	void shouldFillOnUpdate() {
		// Update case
		UpdateEntity updateEntity = new UpdateEntity();
		when(metaObject.getOriginalObject()).thenReturn(updateEntity);

		handler.updateFill(metaObject);
		verify(metaObject).setValue(eq("signValue"), eq(MOCK_SIGN_VALUE));
	}

	@Data
	public static class InsertUpdateEntity {

		private String name;

		private String role;

		@SignField(signer = MockSigner.class, when = { SignField.When.Insert, SignField.When.Update })
		private String signValue;

	}

	@Data
	public static class InsertEntity {

		private String name;

		private String role;

		@SignField(signer = MockSigner.class, when = { SignField.When.Insert })
		private String signValue;

	}

	@Data
	public static class UpdateEntity {

		private String name;

		private String role;

		@SignField(signer = MockSigner.class, when = { SignField.When.Update })
		private String signValue;

	}

	@Data
	public static class EmptyEntity {

		private String name;

		private String role;

		@SignField(signer = MockSigner.class, when = {})
		private String signValue;

	}

}
