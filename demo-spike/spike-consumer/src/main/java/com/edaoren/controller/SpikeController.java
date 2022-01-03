package com.edaoren.controller;

import com.edaoren.api.dto.*;
import com.edaoren.api.entity.User;
import com.edaoren.api.service.SpikeService;
import com.edaoren.api.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author EDaoren
 */
@RestController
@RequestMapping("/spike")
public class SpikeController {

    @DubboReference(timeout = 100000, retries = 0)
    private SpikeService spikeService;



    /**
     * 预热秒杀缓存
     *
     * @param warmUpSpikeParamDTO
     * @return
     */
    @PostMapping("/reBuildSeckillRedis")
    public RestResult<String> reBuildSeckillRedis(@RequestBody WarmUpSpikeParamDTO warmUpSpikeParamDTO) {
        return spikeService.warmUpSpikeActivityData(warmUpSpikeParamDTO);
    }

    /**
     * 获取秒杀活动场次
     *
     * @param
     * @return
     */
    @PostMapping("/getSpikeActivityEvents")
    public RestResult<SpikeActivityInfoDTO> getSpikeActivityEvents() {
        return spikeService.getSpikeActivityEvents();
    }


    /**
     * 获取场次秒杀商品
     *
     * @param
     * @return
     */
    @PostMapping("/getSpikeEventsGoods")
    public RestResult<List<SpikeEventGoodsDTO>> getSpikeEventsGoods(@RequestBody SpikeGoodsParamDTO param) {
        return spikeService.getSpikeEventsGoods(param);
    }

    /**
     * 获取秒杀详情
     *
     * @param
     * @return
     */
    @PostMapping("/getSpikeActivityDetail")
    public RestResult<SpikeActivityDetailDTO> getSpikeActivityDetail(@RequestBody SpikeDetailParamDTO param) {
        return spikeService.getSpikeActivityDetail(param);
    }

    /**
     * 秒杀抢购
     *
     * @param
     * @return
     */
    @PostMapping("/rushBuy")
    public RestResult<SpikeRushBuyResultDTO> rushBuy(@RequestBody SpikeRushBuyParamDTO param) {
        return spikeService.rushBuy(param);
    }

}
