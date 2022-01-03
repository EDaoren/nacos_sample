-- 获取redisKey
local spikeGoodsStocksKey = KEYS[1]
local spikeStatisticsEveryDayKey = KEYS[2]
local spikeStatisticsCollectKey = KEYS[3]
local spikeMemberFlag = KEYS[4]

local goodsId = tonumber(ARGV[1]);
local memberId = tonumber(ARGV[2]);
local buyNum = tonumber(ARGV[3]);

-- 每人限领总量
local memberReceiveLimit = tonumber(ARGV[4]);
-- 每人每日限领量
local dayReceiveLimit = tonumber(ARGV[5]);

redis.log(redis.LOG_NOTICE, "Lua Start!");
redis.log(redis.LOG_NOTICE, goodsId);
redis.log(redis.LOG_NOTICE, memberId);
redis.log(redis.LOG_NOTICE, buyNum);
redis.log(redis.LOG_NOTICE, memberReceiveLimit);
redis.log(redis.LOG_NOTICE, dayReceiveLimit);
if not goodsId or not memberId or not buyNum or buyNum == 0 then
    redis.log(redis.LOG_NOTICE, "here.....");
    return 0;
end

-- 校验库存
local availableInventory = tonumber(redis.call("HGET", spikeGoodsStocksKey, goodsId))
redis.log(redis.LOG_NOTICE, spikeGoodsStocksKey);
if not availableInventory or availableInventory <= 0 then
    redis.log(redis.LOG_NOTICE, "测试日志打印3：");
    return 0;
end

if buyNum > availableInventory then
    return 0;
end

-- 校验是否限购
-- 校验库存
local curDayCount = tonumber(redis.call("HGET", spikeStatisticsEveryDayKey, goodsId));
if not curDayCount then
    curDayCount = 0;
end

local totalCount = tonumber(redis.call("HGET", spikeStatisticsCollectKey, goodsId));

if not curDayCount then
    totalCount = 0;
end

if dayReceiveLimit ~= 0 and (buyNum + curDayCount) > dayReceiveLimit then
    return 0;
end

if memberReceiveLimit ~= 0 and (buyNum + totalCount) > memberReceiveLimit then
    return 0;
end
-- 预扣库存
redis.call('HINCRBY', spikeGoodsStocksKey, goodsId, buyNum * -1);
-- 创建秒杀标记
redis.call('HSET', spikeMemberFlag, memberId, 1);
redis.log(redis.LOG_NOTICE, "Lua end!");
return 1;