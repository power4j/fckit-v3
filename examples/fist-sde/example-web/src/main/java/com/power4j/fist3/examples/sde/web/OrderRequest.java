package com.power4j.fist3.examples.sde.web;

import java.math.BigDecimal;

public class OrderRequest {

	private String orderNo;

	private BigDecimal amount;

	public OrderRequest() {
	}

	public OrderRequest(String orderNo, BigDecimal amount) {
		this.orderNo = orderNo;
		this.amount = amount;
	}

	public String getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "OrderRequest{orderNo='" + this.orderNo + "', amount=" + this.amount + "}";
	}

}
