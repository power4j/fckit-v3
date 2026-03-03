package com.power4j.fist3.examples.logback.extension;

import com.power4j.fist.logback.api.Detector;
import com.power4j.fist.logback.api.LogMessageContext;
import com.power4j.fist.logback.api.MatchSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义识别器：识别18位身份证号
 */
public class IdCardDetector implements Detector {

    private static final Pattern ID_CARD = Pattern.compile("\\d{17}[\\dXx]");

    @Override
    public List<MatchSpan> detect(String message, LogMessageContext context) {
        List<MatchSpan> spans = new ArrayList<>();
        Matcher m = ID_CARD.matcher(message);
        while (m.find()) {
            spans.add(new MatchSpan(m.start(), m.end()));
        }
        return spans;
    }

}
