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

import com.power4j.fist.mybatis.extension.api.sign.DefaultRegistry;
import com.power4j.fist.mybatis.extension.api.sign.SignField;
import com.power4j.fist.mybatis.extension.api.sign.Signer;
import com.power4j.fist.mybatis.extension.handler.MockSigner;
import com.power4j.fist.mybatis.extension.handler.SignFieldMetaObjectHandler;
import com.power4j.fist.mybatis.extension.meta.annotation.FillWith;
import lombok.Data;
import org.apache.ibatis.reflection.MetaObject;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
class MetaHandlerComposeTest {
    private static final String MOCK_META_VALUE = "mocked_meta_value";

    @InjectMocks
    private MetaHandlerCompose metaHandlerCompose;

    @Mock
    private MetaHandlerRegistry handlerRegistry;

    @Mock
    private MetaObject metaObject;

    @Mock
    private MetaHandler mockMetaHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockMetaHandler.apply(any(),any())).thenReturn(MOCK_META_VALUE);
        when(handlerRegistry.resolve(any())).thenReturn(Optional.of(mockMetaHandler));
    }

    @Test
    void shouldThrowExceptionWhenMetaHandlerNotFound() {
        MetaHandlerCompose metaHandlerCompose = new MetaHandlerCompose();
        metaHandlerCompose.insertFill(null);
    }


    @Data
    public static class Foo {

        private String name;

        @FillWith(handler = MockHandler.class)
        private String meta;

    }

    @Data
    public static class Bar {

        private String name;

        @FillWith(handler = MockHandler.class)
        private String meta;

    }

    public static class MockHandler implements MetaHandler {

        @Override
        public Object apply(Object root, String fieldName) {
            return null;
        }
    }
}
