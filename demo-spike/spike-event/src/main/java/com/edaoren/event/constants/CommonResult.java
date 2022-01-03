package com.edaoren.event.constants;

import lombok.Data;

/**
 * 响应结果类
 *
 * @author EDaoren
 */
@Data
public class CommonResult<T> {

    /**
     * 状态
     */
    private long status;

    /**
     * 消息
     */
    private String message;

    /**
     * 结果
     */
    private T data;

    protected CommonResult() {
    }

    protected CommonResult(long status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }


    /**
     * 成功返回结果
     *
     * @param
     * @return
     */
    public static <T> CommonResult<T> success() {
        return new CommonResult<T>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), null);
    }


    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(ResultCode.SUCCESS.code(), message, data);
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(ResultCode.FAILED.code(), message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> failed() {
        return failed(ResultCode.FAILED.message());
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed() {
        return failed(ResultCode.VALIDATE_FAILED.message());
    }

    /**
     * 参数验证失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return new CommonResult<T>(ResultCode.VALIDATE_FAILED.code(), message, null);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized(T data) {
        return new CommonResult<T>(ResultCode.UNAUTHORIZED.code(), ResultCode.UNAUTHORIZED.message(), data);
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden(T data) {
        return new CommonResult<T>(ResultCode.FORBIDDEN.code(), ResultCode.FORBIDDEN.message(), data);
    }
}
