package com.okcoin.commons.okex.open.api.bean.spot.result;

import com.google.gson.annotations.SerializedName;

/**
 * 下单结果
 * https://www.okex.me/docs/zh/#spot-orders
 */
public class OrderResult {

    private boolean result;
    private Long order_id;
    private String client_oid;

    @SerializedName("error_code")
    private String errorCode;

    @SerializedName("error_message")
    private String errorMessage;

    public String getClient_oid() {
        return this.client_oid;
    }

    public void setClient_oid(final String client_oid) {
        this.client_oid = client_oid;
    }

    public boolean isResult() {
        return this.result;
    }

    public void setResult(final boolean result) {
        this.result = result;
    }

    public Long getOrder_id() {
        return this.order_id;
    }

    public void setOrder_id(final Long order_id) {
        this.order_id = order_id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
