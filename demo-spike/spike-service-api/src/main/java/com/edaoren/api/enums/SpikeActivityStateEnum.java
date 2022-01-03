package com.edaoren.api.enums;

/**
 * @author EDaoren
 */
public enum SpikeActivityStateEnum {
    // 秒杀状态状态类型（停用只用于前端显示）
    DISABLE(0, "停用"),
    NOTRECEIVE(1, "即将开抢"),
    NORMAL(2, "进行中"),
    EXPIRED(3, "已结束");

    /**
     * 枚举值
     */
    private Integer value;

    /**
     * 描述
     */
    private String msg;

    SpikeActivityStateEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public String msg() {
        return msg;
    }

    public Integer value() {
        return value;
    }
}
