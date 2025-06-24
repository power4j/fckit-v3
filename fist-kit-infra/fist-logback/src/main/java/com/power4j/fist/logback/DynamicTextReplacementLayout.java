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

package com.power4j.fist.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author CJ (power4j@outlook.com)
 * @since 3.8
 */
public class DynamicTextReplacementLayout extends PatternLayout {

	private final static String CLS_NAME = DynamicTextReplacementLayout.class.getSimpleName();

	private final List<ReplacementProcessor> processors = new ArrayList<>();

	private final StatusManager statusManager;

	public DynamicTextReplacementLayout() {
		LoggerContext lc = (LoggerContext) getContext();
		this.statusManager = lc.getStatusManager();

		statusManager
			.add(new InfoStatus(CLS_NAME + " constructor called. Attempting to load ReplacementProcessors...", this));

		ServiceLoader<ReplacementProcessor> serviceLoader = ServiceLoader.load(ReplacementProcessor.class);
		int loadedCount = 0;
		for (ReplacementProcessor processor : serviceLoader) {
			try {
				processors.add(processor);
				statusManager.add(new InfoStatus(
						"Successfully loaded and added ReplacementProcessor: " + processor.getClass().getName(), this));
				loadedCount++;
			}
			catch (Exception e) {
				statusManager.add(new ErrorStatus(
						"ERROR loading ReplacementProcessor " + processor.getClass().getName() + ": " + e.getMessage(),
						this, e));
			}
		}

		if (loadedCount == 0) {
			statusManager.add(new WarnStatus("No ReplacementProcessor implementations found via ServiceLoader.", this));
		}
		else {
			statusManager.add(new InfoStatus("Finished loading " + loadedCount + " ReplacementProcessors.", this));
		}
	}

	@Override
	public String doLayout(ILoggingEvent event) {
		String message = super.doLayout(event);

		for (ReplacementProcessor processor : processors) {
			Pattern pattern = processor.getPattern();
			Matcher matcher = pattern.matcher(message);
			if (matcher.find()) {
				StringBuffer sb = new StringBuffer();
				matcher.reset();
				while (matcher.find()) {
					String originalMatch = matcher.group();
					String replacedText = processor.replaceMatch(originalMatch);
					matcher.appendReplacement(sb, Matcher.quoteReplacement(replacedText));
				}
				matcher.appendTail(sb);
				message = sb.toString();

				break;
			}
		}

		return message;
	}

}
