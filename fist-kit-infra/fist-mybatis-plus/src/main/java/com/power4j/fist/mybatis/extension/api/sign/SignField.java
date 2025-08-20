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

package com.power4j.fist.mybatis.extension.api.sign;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface SignField {

	/** 实现签名值计算的类 */
	Class<? extends Signer> signer();

	/** 填充时机 */
	When[] when() default { When.Insert, When.Update };

	enum When {

		/** 在插入时填充 */
		Insert,
		/**
		 * 在更新时填充
		 * <p>
		 * 注意:更新时必须现查询出完整的对象,因此MyBatis Plus的LambdaWrapper更新无法触发自动填充
		 */
		Update

	}

}
