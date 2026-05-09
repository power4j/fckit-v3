package com.power4j.fist.sde.core.replay;

/**
 * SDE 重放校验契约。
 */
public interface ReplayGuard {

	/**
	 * 校验并记录 nonce。
	 * @param context 重放校验上下文
	 */
	void checkAndMark(ReplayContext context);

}
