package com.edaoren.api.enums;

/**
 * 秒杀申请状态枚举
 *
 * @author chenchuwen
 * @date 2021/12/27 19:01
 */
public enum SpikeProcessEnum {

    //抢购流程没有这个状态，仅用于前端展示
    SPIKE_UNWANTED(0, "用户尚未提交秒杀抢购请求"),

    // 用户秒杀申请状态
    SPIKE_ING(1, "秒杀中"),
    SPIKE_SUCCESS(2, "秒杀成功"),
    SPIKE_FAIL(3, "秒杀失败");

    /**
     * 枚举值
     */
    private Integer value;

    /**
     * 描述
     */
    private String msg;

    SpikeProcessEnum(int value, String msg) {
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
