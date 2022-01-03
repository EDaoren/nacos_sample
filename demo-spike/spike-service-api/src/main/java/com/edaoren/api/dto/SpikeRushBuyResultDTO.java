package com.edaoren.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 秒杀抢购结果
 *
 * @author chenchuwen
 * @date 2021/12/29 11:32
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpikeRushBuyResultDTO implements Serializable {
    private static final long serialVersionUID = 1045517755264723971L;

    /**
     * 抢购状态
     */
    private Integer buyState;

    /**
     * 信息
     */
    private String message;

}
