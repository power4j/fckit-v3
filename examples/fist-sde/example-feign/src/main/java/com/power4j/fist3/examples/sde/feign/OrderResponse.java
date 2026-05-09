package com.power4j.fist3.examples.sde.feign;

import java.math.BigDecimal;

public class OrderResponse {

	private String orderNo;

	private String status;

	private BigDecimal acceptedAmount;

	public OrderResponse() {
	}

	public OrderResponse(String orderNo, String status, BigDecimal acceptedAmount) {
		this.orderNo = orderNo;
		this.status = status;
		this.acceptedAmount = acceptedAmount;
	}

	public String getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getAcceptedAmount() {
		return this.acceptedAmount;
	}

	public void setAcceptedAmount(BigDecimal acceptedAmount) {
		this.acceptedAmount = acceptedAmount;
	}

	@Override
	public String toString() {
		return "OrderResponse{orderNo='" + this.orderNo + "', status='" + this.status + "', acceptedAmount="
				+ this.acceptedAmount + "}";
	}

}
