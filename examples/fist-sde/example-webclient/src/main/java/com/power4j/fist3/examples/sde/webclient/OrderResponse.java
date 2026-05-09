package com.power4j.fist3.examples.sde.webclient;

class OrderResponse {

	private String orderNo;

	private String status;

	OrderResponse() {
	}

	OrderResponse(String orderNo, String status) {
		this.orderNo = orderNo;
		this.status = status;
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

}
