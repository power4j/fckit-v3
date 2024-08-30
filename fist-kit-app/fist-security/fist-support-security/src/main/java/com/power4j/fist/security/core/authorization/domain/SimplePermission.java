package com.power4j.fist.security.core.authorization.domain;

import org.springframework.util.Assert;

import java.io.Serial;

/**
 * @author CJ (power4j@outlook.com)
 * @since 1.0
 */
public class SimplePermission implements GrantedPermission {

	@Serial
	private static final long serialVersionUID = 1L;

	private final String code;

	public SimplePermission(String code) {
		Assert.hasText(code, "A granted authority textual representation is required");
		this.code = code;
	}

	@Override
	public String getPermissionCode() {
		return this.code;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof SimplePermission) {
			return this.code.equals(((SimplePermission) obj).code);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

	@Override
	public String toString() {
		return this.code;
	}

}
