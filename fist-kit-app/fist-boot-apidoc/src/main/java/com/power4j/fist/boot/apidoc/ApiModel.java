package com.power4j.fist.boot.apidoc;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/8/26
 * @since 1.0
 */
@Data
public class ApiModel implements Serializable {

	@Serial
	private final static long serialVersionUID = 1L;

	/** tag */
	private List<String> docTags;

	/** 服务名 */
	private String serviceName;

	/** 资源ID(服务唯一) */
	private String resourceId;

	/** 鉴权编码 */
	private String code;

	/** HTTP方法 小写 */
	private String method;

	/** 路径 */
	private String path;

	/** 执行动作 类方法名称 */
	private String action;

	/** API描述 */
	private String description;

	/** API 级别 */
	private String level;

	/** 资源暴露选项 0 默认 1 只能内部访问 2登录即可访问 3 公开访问 */
	private String expose;

	/** 是否签名 */
	private boolean sign;

}
