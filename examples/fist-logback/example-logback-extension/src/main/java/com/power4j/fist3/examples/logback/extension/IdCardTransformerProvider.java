package com.power4j.fist3.examples.logback.extension;

import com.power4j.fist.logback.api.Transformer;
import com.power4j.fist.logback.spi.TransformerProvider;

public class IdCardTransformerProvider implements TransformerProvider {

    @Override
    public String name() {
        return "idCardMask";
    }

    @Override
    public Transformer create() {
        return new IdCardTransformer();
    }

}
