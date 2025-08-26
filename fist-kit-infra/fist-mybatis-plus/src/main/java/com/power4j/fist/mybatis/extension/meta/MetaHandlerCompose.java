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

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.power4j.fist.mybatis.extension.exception.MetaHandlerException;
import com.power4j.fist.mybatis.extension.meta.annotation.FillWith;
import lombok.Getter;
import org.apache.ibatis.reflection.MetaObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.9
 */
public class MetaHandlerCompose implements MetaObjectHandler {
    private final MetaHandlerResolver resolver;

    public MetaHandlerCompose(MetaHandlerResolver resolver) {
        this.resolver = resolver;
    }
    @Override
    public void insertFill(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        List<FillTarget> fillTargets = getFillTargets(originalObject);
        for (FillTarget fillTarget : fillTargets) {
            fillInsertValue(metaObject, originalObject, fillTarget);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        List<FillTarget> fillTargets = getFillTargets(originalObject);
        for (FillTarget fillTarget : fillTargets) {
            fillUpdateValue(metaObject, originalObject, fillTarget);
        }
    }

    protected List<FillTarget> getFillTargets(Object originalObject) {
        Class<?> clazz = originalObject.getClass();
        List<FillTarget> annotations = new ArrayList<>(4);
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(FillWith.class)) {
                FillWith fillWith = field.getAnnotation(FillWith.class);
                annotations.add(FillTarget.of(fillWith.order(),fillWith,field));
            }
        }
        return  annotations.stream().sorted(Comparator.comparingInt(FillTarget::getOrder)).collect(Collectors.toList());
    }

    protected void fillValue(MetaObject metaObject, Object originalObject, FillTarget fillTarget,
                           BiConsumer<MetaObject, Supplier<Object>> fillOperation) {
        try {
            Class<? extends MetaHandler> handlerClass = fillTarget.getAnnotation().handler();
            MetaHandler handler = resolver.resolve(handlerClass).orElse(null);
            if (handler == null) {
                throw new MetaHandlerException("could not resolve meta handler object: " + handlerClass.getName());
            }
            String fieldName = fillTarget.field.getName();
            fillOperation.accept(metaObject, () -> handler.apply(originalObject, fieldName));
        } catch (Exception e) {
            throw new MetaHandlerException(e);
        }
    }

    protected void fillInsertValue(MetaObject metaObject, Object originalObject, FillTarget fillTarget) {
        fillValue(metaObject, originalObject, fillTarget,
                (mo, value) -> strictInsertFill(mo, fillTarget.field.getName(), value, Object.class));
    }

    protected void fillUpdateValue(MetaObject metaObject, Object originalObject, FillTarget fillTarget) {
        fillValue(metaObject, originalObject, fillTarget,
                (mo, value) -> strictUpdateFill(mo, fillTarget.field.getName(), value, Object.class));
    }


    @Getter
    static class FillTarget{
        private final int order;
        private final FillWith annotation;
        private final Field field;

        FillTarget(int order, FillWith annotation, Field field) {
            this.order = order;
            this.annotation = annotation;
            this.field = field;
        }

        static  FillTarget of(int order,FillWith annotation, Field field) {
            return new FillTarget(order,annotation, field);
        }
    }
}
