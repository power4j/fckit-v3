package com.power4j.fist3.examples.logback.extension;

import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.Transformer;

/**
 * 自定义转换器：保留前6位（地区码）和后4位，中间用 * 替换
 */
public class IdCardTransformer implements Transformer {

    @Override
    public String transform(String value, LogMessageContext context) {
        if (value.length() != 18) {
            return "******************";
        }
        return value.substring(0, 6) + "********" + value.substring(14);
    }

}
