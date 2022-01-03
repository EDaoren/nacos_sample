package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author EDaoren
 */
@Data
public class SpikeDetailParamDTO implements Serializable {

    /**
     * 秒杀活动ID
     */
    private Long spikeId;

    /**
     * 秒杀商品ID
     */
    private Long goodsId;

    /**
     * 会员ID
     */
    private Long userId;
}
