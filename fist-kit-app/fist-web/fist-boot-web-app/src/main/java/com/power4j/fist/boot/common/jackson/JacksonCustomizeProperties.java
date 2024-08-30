package com.power4j.fist.boot.common.jackson;

import com.power4j.coca.kit.common.datetime.DateTimePattern;
import com.power4j.fist.boot.common.prop.PropConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/5/31
 * @since 1.0
 */
@Data
@ConfigurationProperties(prefix = JacksonCustomizeProperties.PROP_PREFIX)
public class JacksonCustomizeProperties {

	public static final String PROP_ENTRY = "jackson.customize";

	public static final String PROP_PREFIX = PropConstant.PROP_ROOT_PREFIX + PROP_ENTRY;

	public static final String TZ_SYSTEM = "system";

	/**
	 * 关闭时所有增强功能的开关
	 */
	private boolean enabled = true;

	/**
	 * Obfuscation 注解支持
	 */
	private boolean obfuscatedSupport = true;

	/**
	 * 排除的自定义模块
	 */
	@Nullable
	private List<ModuleName> excludeModules = Collections.emptyList();

	/**
	 * 设置默认时间格式,留空则使用Jackson默认值
	 */
	@Nullable
	private String simpleDateFormat = DateTimePattern.DATETIME;

	/**
	 * 设置默认时区,比如 {@code 'Asia/Shanghai'},特殊值 system 表示使用系统时区,留空则使用Jackson默认值(UTC)
	 * @see <a
	 * href=https://docs.oracle.com/cd/E84527_01/wcs/tag-ref/MISC/TimeZones.html>TimeZones</url>
	 */
	@Nullable
	private String timeZoneId = TZ_SYSTEM;

	public enum ModuleName {

		/** 日期、时间相关 */
		Date,
		/** 大整数转String */
		NumberToStr

	}

}
