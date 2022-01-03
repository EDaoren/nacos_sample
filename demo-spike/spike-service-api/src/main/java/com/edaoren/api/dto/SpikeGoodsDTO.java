package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀活动DTO
 *
 * @author chenchuwen
 * @date 2021/12/27 14:39
 */
@Data
public class SpikeGoodsDTO implements Serializable {

    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 库存
     */
    private Integer stocks;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 秒杀ID
     */
    private Long spikeId;

}
