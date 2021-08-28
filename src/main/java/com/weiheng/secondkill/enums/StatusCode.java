package com.weiheng.secondkill.enums;

public enum StatusCode {
    SUCCESS(1000, "SUCCESS"),
    FAIL(1001, "系统开小差"),
    InvalidParams(201, "非法的参数!"),
    UserNotLogin(202, "用户没登录"),
    MqPublishError(203, "消息异步发送失败！"),
    ;
    private Integer code;
    private String msg;

    StatusCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "StatusCode{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
