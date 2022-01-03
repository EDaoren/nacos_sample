package com.edaoren.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀预热数据
 *
 * @author EDaoren
 */
@Data
public class WarmUpSpikeParamDTO implements Serializable {

    /**
     * 秒杀ID
     */
    private Integer spikeId;

}
