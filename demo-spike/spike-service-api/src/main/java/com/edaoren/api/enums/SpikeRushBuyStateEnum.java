package com.edaoren.api.enums;

/**
 * 秒杀抢购状态
 *
 * @author chenchuwen
 * @date 2021/12/27 19:01
 */
public enum SpikeRushBuyStateEnum {

    // 优惠券状态类型
    SPIKE_READY(1, "秒杀未开始"),
    SPIKE_ING(2, "秒杀中"),
    SOLD_OUT(3, "已售罄"),
    RESTRICTED(4, "已限购"),
    SPIKE_FAIL(5, "抢购失败"),
    SPIKE_SUCCESS(6, "抢购成功"),
    SPIKE_END(7, "秒杀已结束");


    /**
     * 枚举值
     */
    private int value;

    /**
     * 描述
     */
    private String msg;

    SpikeRushBuyStateEnum(int value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public String msg() {
        return msg;
    }


    public int value() {
        return value;
    }
}
