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

package com.power4j.fist.mybatis.extension.meta.annotation;

import com.power4j.fist.mybatis.extension.meta.MetaHandler;

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
public @interface FillWith {
    int LOWEST_ORDER = Integer.MAX_VALUE;
    int HIGHEST_ORDER = Integer.MIN_VALUE;

    /**
     * The priority order for filling this field
     * <p>
     *     The lower the value, the higher the priority,{@code LOWEST_ORDER} is the highest priority
     * </p>
     */
    int order() default 0;

    /** Handler class */
    Class<? extends MetaHandler> handler();
}
