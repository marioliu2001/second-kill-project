package com.seckill.common;

/**
 * @Description TODO 统一json返回值对象
 * @Date 2022/2/28 14:42
 * @Version 1.0
 */
public class ReturnObject <T>{
    private int code;//请求状态码 0表示请求成功，1表示请求失败
    private String msg;//请求的消息
    private T result;//具体响应数据

    public ReturnObject() {
    }

    public ReturnObject(int code, String msg, T result) {
        this.code = code;
        this.msg = msg;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
