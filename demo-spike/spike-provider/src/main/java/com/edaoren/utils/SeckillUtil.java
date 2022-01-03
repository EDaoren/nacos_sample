package com.edaoren.utils;

/**
 * 秒杀工具
 *
 * @author chenchuwen
 * @date 2021/12/28 11:58
 */
public class SeckillUtil {

    /**
     * 冒号
     */
    private final static String COLON = ":";

    /**
     * 秒杀活动
     */
    public final static String SPIKE_INFO = "spike-activity:spike-info";

    /**
     * 秒杀商品
     */
    public final static String SPIKE_GOODS = "spike-activity:spike-goods";

    /**
     * 秒杀商品库存
     */
    public final static String SPIKE_GOODS_STOCKS = "spike-activity:spike-goods-stocks";

    /**
     * 用户秒杀申请标记
     */
    public final static String MEMBER_SPIKE_FLAG = "spike-activity:member-spike-flag";

    /**
     * 用户秒杀记录
     */
    public final static String MEMBER_SPIKE_RECORD = "spike-activity:member-spike-record";

    /**
     * 用户秒杀每天统计
     */
    public final static String MEMBER_SPIKE_STATISTICS_EVERYDAY = "spike-activity:member-spike-statistics:everyday";

    /**
     * 用户秒杀汇总统计
     */
    public final static String MEMBER_SPIKE_STATISTICS_COLLECT = "spike-activity:member-spike-statistics:collect";


    /**
     * 获取Redis Key
     *
     * @param prefix     前缀
     * @param businessId 业务ID
     * @return
     */
    public static String getRedisKey(String prefix, Long businessId) {
        return prefix + COLON + businessId;
    }

    /**
     * 获取Redis Key
     *
     * @param prefix        前缀
     * @param businessId    业务ID
     * @param businessIdTow 业务ID2
     * @return
     */
    public static String getRedisKeyByMultipleId(String prefix, Long businessId, Long businessIdTow) {
        return prefix + COLON + businessId + COLON + businessIdTow;
    }
}
