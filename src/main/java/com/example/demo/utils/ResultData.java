package com.example.demo.utils;

import lombok.Data;

import java.io.Serializable;
/**
 * @Author: gyzhang
 * @Date: 2020/10/10 17:43
 */
@Data
public class ResultData implements Serializable {

    private boolean flag;
    private String msg;
    private Object data;

    /**
     * 封装成功信息
     * @param data
     * @return
     */
    public static ResultData SUCCESS(Object data) {
        return resultData(Boolean.TRUE, Constants.SUCCESS_MSG, data);
    }

    /**
     * 封装成功信息
     * @param msg
     * @param data
     * @return
     */
    public static ResultData SUCCESS(String msg, Object data) {
        return resultData(Boolean.TRUE, msg, data);
    }
    /**
     * 封装失败信息
     * @return
     */
    public static ResultData FAILURE() {
        return resultData(Boolean.FALSE, Constants.FAILURE_MSG, null);
    }
    /**
     * 封装失败信息
     * @param data
     * @return
     */
    public static ResultData FAILURE(Object data) {
        return resultData(Boolean.FALSE, Constants.FAILURE_MSG, data);
    }

    /**
     * 封装失败信息
     * @param msg
     * @return
     */
    public static ResultData FAILURE(String msg) {
        return resultData(Boolean.FALSE, msg, null);
    }

    /**
     * 封装返回信息
     * @param flag
     * @param msg
     * @param data
     * @return
     */
    private static ResultData resultData(boolean flag, String msg, Object data) {
        ResultData rd = new ResultData();
        rd.setFlag(flag);
        rd.setMsg(msg);
        rd.setData(data);
        return rd;
    }

}
