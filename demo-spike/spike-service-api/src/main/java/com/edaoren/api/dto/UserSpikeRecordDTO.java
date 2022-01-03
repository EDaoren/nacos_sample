package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户秒杀记录
 *
 * @author chenchuwen
 * @date 2021/12/27 18:52
 */
@Data
public class UserSpikeRecordDTO implements Serializable {
    private static final long serialVersionUID = 820774452589042417L;

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
