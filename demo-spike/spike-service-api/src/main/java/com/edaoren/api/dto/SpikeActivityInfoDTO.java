package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 秒杀活动信息
 *
 * @author EDaoren
 */
@Data
public class SpikeActivityInfoDTO implements Serializable {

    /**
     * 是否有秒杀
     */
    private Boolean hasSeckill;

    /**
     * 服务器时间
     */
    private Date serverTime;


    /**
     * 秒杀活动场次
     */
    private List<SpikeActivityDTO> spikeActivityList;
}
