package com.edaoren.api.enums;


/**
 * @author EDaoren
 */
public enum SpikeGoodsStateEnum {

    // 优惠券状态类型
    DISABLE(0, "已停用"),
    ABOUT_TO_START(1, "即将开抢"),
    IN_PROGRESS(2, "进行中"),
    SPIKE_ING(3, "秒杀中"),
    EXPIRED(4, "已结束"),
    SOLD_OUT(5, "已售罄"),
    RESTRICTED(6, "已限购");


    private Integer value;

    private String msg;

    SpikeGoodsStateEnum(Integer value, String msg) {
        this.value = value;
        this.msg = msg;
    }

    public Integer value() {
        return value;
    }

    public String msg() {
        return msg;
    }
}
