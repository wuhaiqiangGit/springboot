package com.whq.mvc.common;

public enum CommonErrorCode implements ErrorCode{

    E_100101(100101,"测试异常"),

    UNKNOWN(999999,"未知错误");

    private int code;
    private String desc;

    private CommonErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
