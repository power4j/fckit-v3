package com.power4j.fist.oauth2.extension.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Slf4j
@UtilityClass
public class WebUtil {

	public static final String BASIC_PREFIX = "Basic ";

	/**
	 * 解析 client id
	 * @param header head value
	 * @return Optional with client id
	 */
	public Optional<String> parseBasicClientId(@Nullable String header) {
		if (header == null || !header.startsWith(BASIC_PREFIX)) {
			return Optional.empty();
		}
		byte[] base64Token = header.substring(BASIC_PREFIX.length()).getBytes(StandardCharsets.UTF_8);
		byte[] decoded;
		try {
			decoded = Base64.getDecoder().decode(base64Token);
		}
		catch (IllegalArgumentException e) {
			log.warn("Failed to decode basic authentication token: {}", header);
			return Optional.empty();
		}
		String[] parts = StringUtils.split(new String(decoded, StandardCharsets.UTF_8), ":");
		if (parts.length != 2) {
			log.warn("Invalid basic authentication token: {}", header);
			return Optional.empty();
		}
		return Optional.of(parts[0]);
	}

}
