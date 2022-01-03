package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author EDaoren
 */
@Data
public class SpikeActivityDetailDTO implements Serializable {


    /**
     * 服务器时间
     */
    private Date serviceTime;

    /**
     * 秒杀活动ID
     */
    private Long spikeId;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 状态
     */
    private Integer state;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 库存
     */
    private Integer actualStocks;

    /**
     * 商品状态
     */
    private Integer goodsState;
}
