package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author EDaoren
 */
@Data
public class SpikeGoodsParamDTO implements Serializable {

    /**
     * 秒杀活动ID
     */
    private Long spikeId;

    /**
     * 会员ID
     */
    private Long userId;
}
