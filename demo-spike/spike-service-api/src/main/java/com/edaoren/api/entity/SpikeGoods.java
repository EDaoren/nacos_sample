package com.edaoren.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 秒杀商品
 *
 * @author EDaoren
 */
@Data
@TableName(value = "spike_goods")
public class SpikeGoods implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)//指定自增策略
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
     * 实际库存
     */
    private Integer actualStocks;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 秒杀ID
     */
    private Long spikeId;
}
