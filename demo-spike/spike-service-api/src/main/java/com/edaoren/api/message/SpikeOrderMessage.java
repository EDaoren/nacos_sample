package com.edaoren.api.message;

import lombok.Data;

import java.io.Serializable;

/**
 * @author EDaoren
 */
@Data
public class SpikeOrderMessage implements Serializable {
    
    /**
     * 秒杀活动ID
     */
    private Long spikeId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 会员ID
     */
    private Long userId;

    /**
     * 秒杀序列号
     */
    private String serialNo;

    /**
     * 秒杀商品数量
     */
    private Integer count;
}
