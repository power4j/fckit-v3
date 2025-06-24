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

package com.power4j.fist.cloud.rpc.feign;

import com.power4j.coca.kit.common.text.StringPool;
import com.power4j.fist.boot.security.context.UserContextHolder;
import com.power4j.fist.boot.security.core.SecurityConstant;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
public class UserRelayHandler implements RelayHandler {

	@Override
	public void handle(@Nullable HttpServletRequest request, RequestTemplate template) {

		List<String> tokens = Optional.ofNullable(template.headers().get(SecurityConstant.HEADER_USER_TOKEN_INNER))
			.map(l -> l.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList()))
			.orElse(Collections.emptyList());

		// @formatter:off
        if(tokens.isEmpty()){
            UserContextHolder
                    .getOriginalValue()
                    .ifPresent(v -> {
                        if(log.isTraceEnabled()){
                            log.trace("传递 {} :{}",SecurityConstant.HEADER_USER_TOKEN_INNER,v);
                        }
                        template.header(SecurityConstant.HEADER_USER_TOKEN_INNER, Collections.singletonList(v));
                    });
        }else{
            if(log.isTraceEnabled()){
                String pre = StringUtils.join(tokens, StringPool.COMMA);
                log.trace("{} 已经存在:{}",SecurityConstant.HEADER_USER_TOKEN_INNER,pre);
            }
        }
        // @formatter:on
	}

}
