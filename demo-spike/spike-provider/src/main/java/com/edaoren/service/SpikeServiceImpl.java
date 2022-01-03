package com.edaoren.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edaoren.api.dto.*;
import com.edaoren.api.entity.*;
import com.edaoren.api.enums.SpikeActivityStateEnum;
import com.edaoren.api.enums.SpikeGoodsStateEnum;
import com.edaoren.api.enums.SpikeProcessEnum;
import com.edaoren.api.enums.SpikeRushBuyStateEnum;
import com.edaoren.api.exception.BizException;
import com.edaoren.api.message.SpikeOrderMessage;
import com.edaoren.api.service.SpikeService;
import com.edaoren.config.CustomizeStringRedisSerializer;
import com.edaoren.config.KryoRedisSerializer;
import com.edaoren.event.amqp.TestAmqpEvent;
import com.edaoren.event.message.TestMessage;
import com.edaoren.event.publisher.AmqpEventPublisher;
import com.edaoren.listener.SpikeOrderAmqpEvent;
import com.edaoren.mapper.*;
import com.edaoren.utils.RedisUtil;
import com.edaoren.utils.SeckillUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 秒杀服务
 *
 * @author EDaoren
 */
@Service
@DubboService
@Slf4j
public class SpikeServiceImpl implements SpikeService {

    @Autowired
    private SpikeActivityMapper spikeActivityMapper;

    @Autowired
    private SpikeGoodsMapper spikeGoodsMapper;

    @Autowired
    private UserSpikeRecordMapper userSpikeRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SpikeOrderMapper spikeOrderMapper;

    @Autowired
    private AmqpEventPublisher amqpEventPublisher;

    @Override
    public RestResult<String> warmUpSpikeActivityData(WarmUpSpikeParamDTO warmUpSpikeParamDTO) {
        //查询未开始的秒杀活动信息
        LambdaQueryWrapper<SpikeActivity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ObjectUtil.isNotEmpty(warmUpSpikeParamDTO.getSpikeId()), SpikeActivity::getId, warmUpSpikeParamDTO.getSpikeId());

        List<SpikeActivity> spikeActivities = spikeActivityMapper.selectList(lambdaQueryWrapper);
        if (ObjectUtil.isEmpty(spikeActivities)) {
            log.info("没有需要预热的秒杀活动数据");
            return RestResult.success("没有需要预热的秒杀活动数据");
        }
        List<Long> spikeIds = spikeActivities.stream().map(SpikeActivity::getId).collect(Collectors.toList());
        Map<Long, SpikeActivity> collectMap = spikeActivities.stream().collect(Collectors.toMap(SpikeActivity::getId, e -> e));

        List<SpikeGoods> spikeGoodsList = spikeGoodsMapper.selectBatchIds(spikeIds);
        if (ObjectUtil.isEmpty(spikeGoodsList)) {
            log.info("秒杀活动预热失败，秒杀商品数据为空");
            throw new BizException("秒杀活动预热失败，秒杀商品数据为空");
        }
        Map<Long, List<SpikeGoods>> spikeGoodsGroup = spikeGoodsList.stream().collect(Collectors.groupingBy(SpikeGoods::getSpikeId));

        //秒杀活动存储到redis
        for (SpikeActivity spikeActivity : spikeActivities) {
            SpikeActivityDTO spikeActivityDTO = new SpikeActivityDTO();
            BeanUtil.copyProperties(spikeActivity, spikeActivityDTO);
            RedisUtil.<Long, SpikeActivityDTO>hashRedis().put(SeckillUtil.SPIKE_INFO, spikeActivity.getId(), spikeActivityDTO);
        }

        //秒杀商品存储到redis
        for (Map.Entry<Long, List<SpikeGoods>> entry : spikeGoodsGroup.entrySet()) {
            Long spikeId = entry.getKey();
            //当前秒杀活动
            SpikeActivity spikeActivity = collectMap.get(spikeId);

            //计算缓存过期时间，规则：从现在距离活动过期时间共几个小时，然后延长24小时
            long expire = DateUtil.between(new Date(), spikeActivity.getEndTime(), DateUnit.HOUR) + 24L;

            List<SpikeGoods> curSpikeGoodsList = entry.getValue();
            for (SpikeGoods spikeGoods : curSpikeGoodsList) {
                SpikeGoodsDTO spikeGoodsDTO = new SpikeGoodsDTO();
                BeanUtil.copyProperties(spikeGoods, spikeGoodsDTO);
                String redisGoodsKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS, spikeId);
                RedisUtil.<Long, SpikeGoodsDTO>hashRedis().put(redisGoodsKey, spikeGoods.getId(), spikeGoodsDTO);
                RedisUtil.redis().expire(redisGoodsKey, expire, TimeUnit.HOURS);

                //缓存秒杀商品库存
                String redisStocksKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS_STOCKS, spikeId);
                RedisUtil.<Long, Integer>hashRedis().put(redisStocksKey, spikeGoods.getId(), spikeGoods.getActualStocks());
                RedisUtil.redis().expire(redisStocksKey, expire, TimeUnit.HOURS);
            }
        }
        return RestResult.success("预热缓存成功");
    }

    @Override
    public RestResult<SpikeActivityInfoDTO> getSpikeActivityEvents() {
        SpikeActivityInfoDTO spikeActivityInfoDTO = new SpikeActivityInfoDTO();
        spikeActivityInfoDTO.setHasSeckill(false);
        spikeActivityInfoDTO.setServerTime(new Date());

        //获取秒杀活动场次
        List<SpikeActivityDTO> spikeActivityList = RedisUtil.<Long, SpikeActivityDTO>hashRedis().values(SeckillUtil.SPIKE_INFO);
        if (ObjectUtil.isEmpty(spikeActivityList)) {
            log.info("没有秒杀活动数据，请确认是否已启用秒杀活动");
            return RestResult.success(spikeActivityInfoDTO);
        }

        List<SpikeActivityDTO> eventsList = new ArrayList<>();
        //今天的开始时间
        DateTime beginOfDay = DateUtil.beginOfDay(new Date());
        //今天的结束时间
        DateTime endOfDay = DateUtil.endOfDay(new Date());

        //筛选今天的秒杀活动
        for (SpikeActivityDTO spikeActivityDTO : spikeActivityList) {
            if ((DateUtil.compare(spikeActivityDTO.getStartTime(), beginOfDay) <= 0 && DateUtil.compare(spikeActivityDTO.getEndTime(), beginOfDay) > 0)
                    || (DateUtil.compare(spikeActivityDTO.getStartTime(), beginOfDay) >= 0 && DateUtil.compare(spikeActivityDTO.getStartTime(), endOfDay) <= 0)) {
                eventsList.add(spikeActivityDTO);
            }
        }

        if (ObjectUtil.isNotEmpty(eventsList)) {
            spikeActivityInfoDTO.setHasSeckill(true);
            spikeActivityInfoDTO.setSpikeActivityList(eventsList);
        }
        return RestResult.success(spikeActivityInfoDTO);
    }

    @Override
    public RestResult<List<SpikeEventGoodsDTO>> getSpikeEventsGoods(SpikeGoodsParamDTO param) {
        //获取秒杀活动商品
        String redisGoodsKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS, param.getSpikeId());
        List<SpikeGoodsDTO> redisGoodsList = RedisUtil.<Long, SpikeGoodsDTO>hashRedis().values(redisGoodsKey);
        if (ObjectUtil.isEmpty(redisGoodsList)) {
            log.info("秒杀活动数据异常，该场次商品数据为空，秒杀活动Id：{}", param.getSpikeId());
            throw new BizException("秒杀活动数据异常，该场次商品数据为空");
        }

        List<SpikeEventGoodsDTO> resultList = new ArrayList<>();
        for (SpikeGoodsDTO spikeGoodsDTO : redisGoodsList) {
            SpikeEventGoodsDTO spikeEventGoodsDTO = new SpikeEventGoodsDTO();
            BeanUtil.copyProperties(spikeGoodsDTO, spikeEventGoodsDTO);

            //获取秒杀活动信息
            SpikeActivityDTO spikeActivityDTO = RedisUtil.<Long, SpikeActivityDTO>hashRedis().get(SeckillUtil.SPIKE_INFO, param.getSpikeId());
            if (ObjectUtil.isEmpty(spikeActivityDTO)) {
                log.info("秒杀活动数据异常，秒杀活动数据为空，秒杀活动Id：{}", param.getSpikeId());
                throw new BizException("秒杀活动数据异常，秒杀活动数据为空");
            }
            Integer goodsState = this.decideGoodsState(spikeActivityDTO, spikeGoodsDTO, param.getUserId());
            spikeEventGoodsDTO.setGoodsState(goodsState);
            resultList.add(spikeEventGoodsDTO);
        }
        return RestResult.success(resultList);
    }

    @Override
    public RestResult<SpikeActivityDetailDTO> getSpikeActivityDetail(SpikeDetailParamDTO param) {
        SpikeActivityDetailDTO spikeActivityDetail = new SpikeActivityDetailDTO();
        spikeActivityDetail.setServiceTime(new Date());

        //获取秒杀活动信息
        SpikeActivityDTO spikeActivityDTO = RedisUtil.<Long, SpikeActivityDTO>hashRedis().get(SeckillUtil.SPIKE_INFO, param.getSpikeId());
        if (ObjectUtil.isEmpty(spikeActivityDTO)) {
            log.info("秒杀活动数据异常，秒杀活动数据为空，秒杀活动Id：{}", param.getSpikeId());
            throw new BizException("秒杀活动数据异常，秒杀活动数据为空");
        }
        spikeActivityDetail.setSpikeId(spikeActivityDTO.getId());
        spikeActivityDetail.setStartTime(spikeActivityDTO.getStartTime());
        spikeActivityDetail.setEndTime(spikeActivityDTO.getEndTime());
        spikeActivityDetail.setState(spikeActivityDTO.getState());

        //获取秒杀活动商品
        String redisGoodsKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS, param.getSpikeId());
        SpikeGoodsDTO redisGoods = RedisUtil.<Long, SpikeGoodsDTO>hashRedis().get(redisGoodsKey, param.getGoodsId());
        if (ObjectUtil.isEmpty(redisGoods)) {
            log.info("秒杀活动数据异常，商品数据为空，秒杀活动Id：{}, 秒杀商品ID：{}", param.getSpikeId(), param.getGoodsId());
            throw new BizException("秒杀活动数据异常，商品数据为空");
        }

        spikeActivityDetail.setGoodsId(redisGoods.getId());
        spikeActivityDetail.setGoodsName(redisGoods.getName());

        Integer goodsState = this.decideGoodsState(spikeActivityDTO, redisGoods, param.getUserId());
        spikeActivityDetail.setGoodsState(goodsState);
        return RestResult.success(spikeActivityDetail);
    }

    @Override
    public RestResult<SpikeRushBuyResultDTO> rushBuy(SpikeRushBuyParamDTO param) {
        //获取秒杀活动信息
        SpikeActivityDTO spikeActivityDTO = RedisUtil.<Long, SpikeActivityDTO>hashRedis().get(SeckillUtil.SPIKE_INFO, param.getSpikeId());
        if (ObjectUtil.isEmpty(spikeActivityDTO)) {
            log.info("秒杀活动数据异常，秒杀活动数据为空，秒杀活动Id：{}", param.getSpikeId());
            throw new BizException("秒杀活动数据异常，秒杀活动数据为空");
        }

        if (spikeActivityDTO.getState().equals(SpikeActivityStateEnum.NOTRECEIVE.value())
                || DateUtil.compare(new Date(), spikeActivityDTO.getStartTime()) < 0) {
            SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
            spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SPIKE_READY.value());
            spikeRushBuyResultDTO.setMessage("秒杀活动尚未开始，不要心急哦，请稍后再试");
            return RestResult.success(spikeRushBuyResultDTO);
        }

        if (spikeActivityDTO.getState().equals(SpikeActivityStateEnum.EXPIRED.value())
                || spikeActivityDTO.getState().equals(SpikeActivityStateEnum.DISABLE.value())
                || DateUtil.compare(new Date(), spikeActivityDTO.getEndTime()) > 0) {
            SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
            spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SPIKE_READY.value());
            spikeRushBuyResultDTO.setMessage("秒杀活动已结束，下次要早点来哦");
            return RestResult.success(spikeRushBuyResultDTO);
        }

        //校验商品库存
        String redisGoodsStocksKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS_STOCKS, param.getSpikeId());
        Integer availableInventory = RedisUtil.<Long, Integer>hashRedis().get(redisGoodsStocksKey, param.getGoodsId());
        if (ObjectUtil.isEmpty(availableInventory)) {
            log.info("秒杀活动数据异常, 秒杀商品库存为空，秒杀活动ID{}，秒杀商品ID：{}", param.getSpikeId(), param.getGoodsId());
            throw new BizException("秒杀活动数据异常, 秒杀商品库存为空");
        }

        if (availableInventory <= 0 || availableInventory < param.getBuyNum()) {
            SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
            spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SOLD_OUT.value());
            spikeRushBuyResultDTO.setMessage("抱歉，活动太火爆了，优惠已售罄");
            return RestResult.success(spikeRushBuyResultDTO);
        }

        //校验用户是否秒杀中
        String redisMemberFlagKey = SeckillUtil.getRedisKeyByMultipleId(SeckillUtil.MEMBER_SPIKE_FLAG, param.getSpikeId(), param.getGoodsId());
        Integer spikeProcess = RedisUtil.<Long, Integer>hashRedis().get(redisMemberFlagKey, param.getUserId());
        if (ObjectUtil.isNotEmpty(spikeProcess) && spikeProcess.equals(SpikeProcessEnum.SPIKE_ING.value())) {
            SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
            spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SPIKE_ING.value());
            spikeRushBuyResultDTO.setMessage("别心急~，正在火速抢购中");
            return RestResult.success(spikeRushBuyResultDTO);
        }


        //用户秒杀当天统计
        String redisStatisticsEverydayKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_EVERYDAY, param.getUserId());

        //用户秒杀汇总统计
        String redisStatisticsCollectKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_COLLECT, param.getUserId());

        String serialNo = UUID.fastUUID().toString();


        //执行lua脚本预扣库存
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/SeckillDeductionStock.lua")));
        redisScript.setResultType(Long.class);

        //预加载脚本
        List<String> keys = new ArrayList<>();
        keys.add(redisGoodsStocksKey);
        keys.add(redisStatisticsEverydayKey);
        keys.add(redisStatisticsCollectKey);
        keys.add(redisMemberFlagKey);

        Map<String, Object> argvMap = new HashMap<String, Object>();
        argvMap.put("spikeId", param.getGoodsId());
        argvMap.put("goodsId", param.getGoodsId());
        argvMap.put("buyNum", param.getBuyNum());
        argvMap.put("memberId", param.getUserId());
        argvMap.put("memberReceiveLimit", 10);
        argvMap.put("dayReceiveLimit", 20);

        String name = "Tset";

        //Lua无法接收 Long 类型的值，目前不知道什么原因
        Long result = RedisUtil.redis().execute(redisScript, keys, 1, param.getUserId().intValue(), param.getBuyNum(), 0, 0);

        if (result == 0) {
            log.info("秒杀活动那个预扣库存失败，秒杀ID：{}，商品ID：{}， 用户ID：{}, 抢购数量：{}", param.getSpikeId(),
                    param.getGoodsId(), param.getUserId(), param.getBuyNum());
            //预扣库存失败
            SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
            spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SPIKE_FAIL.value());
            spikeRushBuyResultDTO.setMessage("对不起，活动太火爆了，没抢到哦");
            return RestResult.success(spikeRushBuyResultDTO);
        }
        //创建抢购记录
        UserSpikeRecordDTO userSpikeRecordDTO = new UserSpikeRecordDTO();
        userSpikeRecordDTO.setSpikeId(param.getSpikeId());
        userSpikeRecordDTO.setGoodsId(param.getGoodsId());
        userSpikeRecordDTO.setUserId(param.getUserId());
        userSpikeRecordDTO.setSerialNo(serialNo);
        userSpikeRecordDTO.setCount(param.getBuyNum());

        String redisSpikeRecordKey = SeckillUtil.getRedisKeyByMultipleId(SeckillUtil.MEMBER_SPIKE_RECORD, param.getSpikeId(), param.getGoodsId());
        RedisUtil.<Long, UserSpikeRecordDTO>hashRedis().put(redisSpikeRecordKey, param.getUserId(), userSpikeRecordDTO);

        SpikeOrderMessage spikeOrderMessage = new SpikeOrderMessage();
        BeanUtil.copyProperties(userSpikeRecordDTO, spikeOrderMessage);
        //发送mq信息
        amqpEventPublisher.publish(new SpikeOrderAmqpEvent(spikeOrderMessage), true);

        //预扣库存失败
        SpikeRushBuyResultDTO spikeRushBuyResultDTO = new SpikeRushBuyResultDTO();
        spikeRushBuyResultDTO.setBuyState(SpikeRushBuyStateEnum.SPIKE_ING.value());
        spikeRushBuyResultDTO.setMessage("秒杀排队中，请耐心等待");
        return RestResult.success(spikeRushBuyResultDTO);
    }

    @Override
    public RestResult<String> rushBuySpikeActivityProcess(SpikeOrderMessage spikeRecord) {
        //校验是否已经有秒杀记录
        LambdaQueryWrapper<UserSpikeRecord> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserSpikeRecord::getSerialNo, spikeRecord.getSerialNo());
        UserSpikeRecord record = userSpikeRecordMapper.selectOne(lambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(record)) {
            log.info("秒杀订单已经创建，无需重复创建：，秒杀活动ID：{}， 秒杀序列号：{}", spikeRecord.getSpikeId(), spikeRecord.getSerialNo());
            return RestResult.success("秒杀订单已经创建，无需重复创建");
        }

        //创建订单
        User user = userMapper.selectById(spikeRecord.getUserId());
        if (ObjectUtil.isEmpty(user)) {
            log.info("秒杀活动，用户抢购失败，用户信息为空，秒杀活动ID：{}，秒杀商品ID：{}，会员ID：{}， 抢购数量：{}，", spikeRecord.getSpikeId(),
                    spikeRecord.getGoodsId(), spikeRecord.getUserId(), spikeRecord.getCount());
            rushBuySeckillFailProcess(spikeRecord);
            return RestResult.success("秒杀活动，用户抢购失败，用户信息为空");
        }

        try {

            //校验秒杀活动
            SpikeActivity spikeActivity = spikeActivityMapper.selectById(spikeRecord.getSpikeId());
            if (ObjectUtil.isEmpty(spikeActivity) || !spikeActivity.getState().equals(SpikeActivityStateEnum.NORMAL.value())) {
                log.info("秒杀活动，用户抢购失败，活动信息异常，活动已结束或者停用，秒杀活动ID：{}，秒杀商品ID：{}，会员ID：{}， 抢购数量：{}，", spikeRecord.getSpikeId(),
                        spikeRecord.getGoodsId(), spikeRecord.getUserId(), spikeRecord.getCount());
                rushBuySeckillFailProcess(spikeRecord);
                return RestResult.success("秒杀活动，用户抢购失败，活动信息异常，活动已结束或者停用");
            }

            //校验秒杀商品
            SpikeGoods spikeGoods = spikeGoodsMapper.selectById(spikeRecord.getGoodsId());
            if (ObjectUtil.isEmpty(spikeGoods) || spikeRecord.getCount() > spikeGoods.getActualStocks()) {
                log.info("秒杀活动，用户抢购失败，秒杀商品库为空或商品库存不足，秒杀活动ID：{}，秒杀商品ID：{}，会员ID：{}， 抢购数量：{}，", spikeRecord.getSpikeId(),
                        spikeRecord.getGoodsId(), spikeRecord.getUserId(), spikeRecord.getCount());
                rushBuySeckillFailProcess(spikeRecord);
                return RestResult.success("秒杀活动，用户抢购失败，秒杀商品库为空或商品库存不足");
            }

            //创建订单
            SpikeOrder spikeOrder = new SpikeOrder();
            spikeOrder.setNum(spikeRecord.getCount());
            spikeOrder.setUserId(spikeRecord.getUserId());
            spikeOrder.setProdId(spikeRecord.getGoodsId());
            spikeOrder.setSubNumber(UUID.fastUUID().toString());
            spikeOrder.setCreateTime(new Date());
            int insert = spikeOrderMapper.insert(spikeOrder);

            if (insert <= 0) {
                log.info("秒杀活动，用户抢购失败，用户信息为空，秒杀活动ID：{}，秒杀商品ID：{}，会员ID：{}， 抢购数量：{}，", spikeRecord.getSpikeId(),
                        spikeRecord.getGoodsId(), spikeRecord.getUserId(), spikeRecord.getCount());
                rushBuySeckillFailProcess(spikeRecord);
                return RestResult.success("秒杀活动，用户抢购失败，用户信息为空");
            }

            //扣减库存
            spikeGoods.setActualStocks(spikeGoods.getActualStocks() - spikeRecord.getCount());
            spikeGoodsMapper.update(spikeGoods, null);

            //创建订单成功，修改标记
            String memberSpikeFlag = SeckillUtil.getRedisKeyByMultipleId(SeckillUtil.MEMBER_SPIKE_FLAG, spikeRecord.getSpikeId(), spikeRecord.getGoodsId());
            RedisUtil.<Long, Integer>hashRedis().put(memberSpikeFlag, spikeRecord.getUserId(), SpikeProcessEnum.SPIKE_SUCCESS.value());

            //统计用户秒杀数据-今天
            String redisStatisticsEverydayKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_EVERYDAY, spikeRecord.getUserId());
            RedisUtil.<Long, Integer>hashRedis().increment(redisStatisticsEverydayKey, spikeRecord.getGoodsId(), spikeRecord.getCount());
            //设置key今天过期
            DateTime endDay = DateUtil.endOfDay(new Date());
            long between = DateUtil.between(new Date(), endDay, DateUnit.MINUTE);
            RedisUtil.redis().expire(redisStatisticsEverydayKey, between, TimeUnit.MINUTES);

            //统计用户秒杀数据-所有
            String spikeStatisticsCollectKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_COLLECT, spikeRecord.getUserId());
            RedisUtil.<Long, Integer>hashRedis().increment(spikeStatisticsCollectKey, spikeRecord.getGoodsId(), spikeRecord.getCount());

            //创建秒杀记录
            UserSpikeRecord userSpikeRecord = new UserSpikeRecord();
            userSpikeRecord.setCreateTime(new Date());
            userSpikeRecord.setSerialNo(spikeRecord.getSerialNo());
            userSpikeRecord.setUserId(spikeRecord.getUserId());
            userSpikeRecord.setSpikeId(spikeRecord.getSpikeId());
            userSpikeRecord.setGoodsId(spikeRecord.getGoodsId());
            userSpikeRecord.setCount(spikeRecord.getCount());
            userSpikeRecord.setOrderId(spikeOrder.getId());
            userSpikeRecordMapper.insert(userSpikeRecord);

        } catch (Exception e) {
            rushBuySeckillFailProcess(spikeRecord);
            log.error("创建订单异常:", e);
        }
        return RestResult.success("秒杀活动，用户抢购成功");
    }


    /**
     * 秒杀失败处理
     *
     * @param spikeOrderMessage
     */
    private void rushBuySeckillFailProcess(SpikeOrderMessage spikeOrderMessage) {
        //修改秒杀标记
        String memberSpikeFlag = SeckillUtil.getRedisKeyByMultipleId(SeckillUtil.MEMBER_SPIKE_FLAG, spikeOrderMessage.getSpikeId(), spikeOrderMessage.getGoodsId());
        RedisUtil.<Long, Integer>hashRedis().put(memberSpikeFlag, spikeOrderMessage.getUserId(), SpikeProcessEnum.SPIKE_FAIL.value());

        //统计用户秒杀数据-今天
        String redisStatisticsEverydayKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_EVERYDAY, spikeOrderMessage.getUserId());
        RedisUtil.<Long, Integer>hashRedis().increment(redisStatisticsEverydayKey, spikeOrderMessage.getGoodsId(), spikeOrderMessage.getCount() * -1L);

        //统计用户秒杀数据-所有
        String spikeStatisticsCollectKey = SeckillUtil.getRedisKey(SeckillUtil.MEMBER_SPIKE_STATISTICS_COLLECT, spikeOrderMessage.getUserId());
        RedisUtil.<Long, Integer>hashRedis().increment(spikeStatisticsCollectKey, spikeOrderMessage.getGoodsId(), spikeOrderMessage.getCount() * -1L);

        //回滚库存
        String spikeGoodsStocksKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS_STOCKS, spikeOrderMessage.getSpikeId());
        RedisUtil.<Long, Integer>hashRedis().increment(spikeGoodsStocksKey, spikeOrderMessage.getGoodsId(), spikeOrderMessage.getCount());

    }


    /**
     * 判断秒杀商品状态
     *
     * @return
     */
    private Integer decideGoodsState(SpikeActivityDTO activity, SpikeGoodsDTO goods, Long memberId) {
        //商品状态
        Integer goodState = SpikeGoodsStateEnum.IN_PROGRESS.value();

        //设置秒杀商品活动状态
        if (activity.getState().equals(SpikeActivityStateEnum.NOTRECEIVE.value())) {
            goodState = SpikeGoodsStateEnum.ABOUT_TO_START.value();
        } else if (activity.getState().equals(SpikeActivityStateEnum.NORMAL.value())) {
            goodState = SpikeGoodsStateEnum.IN_PROGRESS.value();
        } else if (activity.getState().equals(SpikeActivityStateEnum.EXPIRED.value())) {
            goodState = SpikeGoodsStateEnum.EXPIRED.value();
        } else if (activity.getState().equals(SpikeActivityStateEnum.DISABLE.value())) {
            goodState = SpikeGoodsStateEnum.DISABLE.value();
        }

        //活动进行中，校验商品其他状态
        if (activity.getState().equals(SpikeActivityStateEnum.NORMAL.value())) {
            //商品是否被停用 TODO 暂时不校验
            //是否正在参加秒杀
            String redisMemberFlagKey = SeckillUtil.getRedisKeyByMultipleId(SeckillUtil.MEMBER_SPIKE_FLAG, activity.getId(), goods.getId());
            Integer spikeProcess = RedisUtil.<Long, Integer>hashRedis().get(redisMemberFlagKey, memberId);

            if (ObjectUtil.isNotEmpty(spikeProcess) && spikeProcess.equals(SpikeProcessEnum.SPIKE_ING.value())) {
                goodState = SpikeGoodsStateEnum.SPIKE_ING.value();
            } else {
                //是否还有库存
                String redisGoodsStocksKey = SeckillUtil.getRedisKey(SeckillUtil.SPIKE_GOODS_STOCKS, activity.getId());
                Integer availableInventory = RedisUtil.<Long, Integer>hashRedis().get(redisGoodsStocksKey, goods.getId());
                if (ObjectUtil.isEmpty(availableInventory)) {
                    log.info("秒杀活动数据异常, 秒杀商品库存为空，秒杀活动ID{}，秒杀商品ID：{}", activity.getId(), goods.getId());
                    throw new BizException("秒杀活动数据异常, 秒杀商品库存为空");
                }

                if (availableInventory <= 0) {
                    goodState = SpikeGoodsStateEnum.SOLD_OUT.value();
                }
            }
        }
        return goodState;
    }
}
