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

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.power4j.fist.mybatis.extension.api.sign.SignField;
import com.power4j.fist.mybatis.extension.api.sign.Signer;
import com.power4j.fist.mybatis.extension.api.sign.SignerResolver;
import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Field;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
@RequiredArgsConstructor
public class SignFieldMetaObjectHandler implements MetaObjectHandler {

	private final SignerResolver signerResolver;

	@Override
	public void insertFill(MetaObject metaObject) {
		Object originalObject = metaObject.getOriginalObject();
		fillSignValue(metaObject, originalObject, SignField.When.Insert);
	}

	@Override
	public void updateFill(MetaObject metaObject) {
		Object originalObject = metaObject.getOriginalObject();
		fillSignValue(metaObject, originalObject, SignField.When.Update);
	}

	private void fillSignValue(MetaObject metaObject, Object originalObject, SignField.When target) {
		Class<?> clazz = originalObject.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(SignField.class)) {
				SignField signField = field.getAnnotation(SignField.class);
				for (SignField.When want : signField.when()) {
					if (want == target) {
						try {
							Signer signer = signerResolver.resolve(signField.signer()).orElse(null);
							if (signer == null) {
								throw new MetaHandlerException("Signer not found: " + signField.signer());
							}
							// 生成签名值
							String signValue = signer.apply(originalObject);
							// 填充字段
							metaObject.setValue(field.getName(), signValue);
						}
						catch (Exception e) {
							throw new MetaHandlerException(e);
						}
						break;
					}
				}
			}
		}
	}

}
