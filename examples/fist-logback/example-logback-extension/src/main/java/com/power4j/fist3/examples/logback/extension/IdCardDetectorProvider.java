package com.power4j.fist3.examples.logback.extension;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.spi.DetectorProvider;

public class IdCardDetectorProvider implements DetectorProvider {

    @Override
    public String name() {
        return "idCard";
    }

    @Override
    public Detector create() {
        return new IdCardDetector();
    }

}
