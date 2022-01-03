package com.edaoren.api.service;


import com.edaoren.api.dto.*;
import com.edaoren.api.message.SpikeOrderMessage;

import java.util.List;

/**
 * 秒杀服务
 *
 * @author EDaoren
 */
public interface SpikeService {

    /**
     * 缓存预热数据
     *
     * @param warmUpSpikeParamDTO
     * @return
     */
    RestResult<String> warmUpSpikeActivityData(WarmUpSpikeParamDTO warmUpSpikeParamDTO);

    /**
     * 获取秒杀活动场次
     *
     * @return
     */
    RestResult<SpikeActivityInfoDTO> getSpikeActivityEvents();

    /**
     * 获取场次秒杀商品
     *
     * @param param
     * @return
     */
    RestResult<List<SpikeEventGoodsDTO>> getSpikeEventsGoods(SpikeGoodsParamDTO param);

    /**
     * 获取秒杀详情
     *
     * @param param
     * @return
     */
    RestResult<SpikeActivityDetailDTO> getSpikeActivityDetail(SpikeDetailParamDTO param);


    /**
     * 秒杀抢购
     *
     * @param param
     * @return
     */
    RestResult<SpikeRushBuyResultDTO> rushBuy(SpikeRushBuyParamDTO param);


    /**
     * 秒杀抢购处理
     *
     * @param seckillRecord
     * @return
     */
    RestResult<String> rushBuySpikeActivityProcess(SpikeOrderMessage seckillRecord);

}
