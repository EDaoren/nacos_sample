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
public class SpikeActivityDTO implements Serializable {

    private Long id;

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
}
