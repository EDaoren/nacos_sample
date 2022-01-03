package com.edaoren.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName(value = "spike_order")
public class SpikeOrder implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)//指定自增策略
    private Long id;

    /**
     * 订单号
     */
    @TableField(value = "sub_number")
    private String subNumber;

    /**
     * 库存
     */
    @TableField(value = "prod_id")
    private Long prodId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 下单数量
     */
    private Integer num;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;
}
