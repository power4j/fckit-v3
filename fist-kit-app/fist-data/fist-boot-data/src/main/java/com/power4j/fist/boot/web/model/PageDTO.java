package com.power4j.fist.boot.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
@Data
public class PageDTO<T> implements Serializable {

	@Serial
	private final static long serialVersionUID = 1L;

	@Schema(description = "数据")
	private List<T> content;

	@Schema(description = "总行数")
	private Integer total;

	@Schema(description = "是否有下一页")
	private Boolean hasNext;

	@Schema(description = "页码")
	private Integer pageNumber;

	@Schema(description = "页大小")
	private Integer pageSize;

}
