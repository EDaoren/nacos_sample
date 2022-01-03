package com.edaoren.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author EDaoren
 */
@Data
@TableName(value = "user_spike_record")
public class UserSpikeRecord implements Serializable {

    /**
     * 秒杀活动ID
     */
    @TableId(value = "id", type = IdType.AUTO)//指定自增策略
    private Long id;

    /**
     * 秒杀ID
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

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 创建时间
     */
    private Date createTime;
}
