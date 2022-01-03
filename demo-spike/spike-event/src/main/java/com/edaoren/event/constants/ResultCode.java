package com.edaoren.event.constants;

/**
 * 响应结果状态
 *
 * @author EDaoren
 */
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(1, "操作成功"),

    /**
     * 失败
     */
    FAILED(-1, "操作失败"),

    /**
     * 参数检验失败
     */
    VALIDATE_FAILED(404, "参数检验失败"),

    /**
     * 暂未登录或token已经过期
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    /**
     * 没有相关权限
     */
    FORBIDDEN(403, "没有相关权限");
    private long code;
    private String message;

    private ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }

    public long code() {
        return code;
    }

    public String message() {
        return message;
    }

}
