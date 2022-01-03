package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀抢购参数
 *
 * @author chenchuwen
 * @date 2021/12/29 11:30
 */
@Data
public class SpikeRushBuyParamDTO implements Serializable {
    private static final long serialVersionUID = -6231619480996978760L;

    /**
     * 秒杀活动ID
     */
    private Long spikeId;

    /**
     * 秒杀商品ID
     */
    private Long goodsId;

    /**
     * 秒杀数量
     */
    private Integer buyNum;

    /**
     * 会员ID
     */
    private Long userId;
}
