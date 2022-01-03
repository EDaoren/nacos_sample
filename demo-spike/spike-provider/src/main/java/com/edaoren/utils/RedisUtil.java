package com.edaoren.utils;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
public class RedisUtil {

    //    private static String keyPrefix = SpringUtil.getProperty("spring.redis.key-prefix");
    public static String keyPrefix = "mdc_";

    public static final int TIMEOUT_30_SECONDS = 30 * 60; // 30分钟
    public static final int TIMEOUT_60_SECONDS = 60 * 60; // 60分钟
    public static final int TIMEOUT_12_HOURS = 12 * 60 * 60; // 12小时
    public static final int TIMEOUT_1_DAY = 24 * 60 * 60; // 一天
    public static final int TIMEOUT_3_DAY = 3 * 24 * 60 * 60; // 3天
    public static final int TIMEOUT_7_DAY = 7 * 24 * 60 * 60; // 7天
    public static final int TIMEOUT_30_DAY = 30 * 24 * 60 * 60; // 7天

    public static <T extends Serializable> RedisTemplate<String, T> generalRedis() {
        return SpringUtil.getBean("generalRedisTemplate");
    }

    public static <T extends Serializable> RedisTemplate<String, T> redis() {
        return SpringUtil.getBean("redisTemplate");
    }

    public static RedissonClient redisson() {
        return SpringUtil.getBean("redisson");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> ValueOperations<String, T> valueRedis() {
        return SpringUtil.getBean("redisTemplate", RedisTemplate.class).opsForValue();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> ListOperations<String, T> listRedis() {
        return SpringUtil.getBean("redisTemplate", RedisTemplate.class).opsForList();
    }

    @SuppressWarnings("unchecked")
    public static <K extends Serializable, V extends Serializable> HashOperations<String, K, V> hashRedis() {
        return SpringUtil.getBean("redisTemplate", RedisTemplate.class).opsForHash();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> SetOperations<String, T> setRedis() {
        return SpringUtil.getBean("redisTemplate", RedisTemplate.class).opsForSet();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> ZSetOperations<String, T> zSetRedis() {
        return SpringUtil.getBean("redisTemplate", RedisTemplate.class).opsForZSet();
    }

    public static void transactionalLock(String lockName) {
        transactionalLock(lockName, 10L);
    }

    /**
     * 事务锁,加锁后当事务结束时才释放锁
     */
    public static void transactionalLock(String lockName, Long lockTime) {
        String tempLockName = keyPrefix + lockName;
        RLock lock = redisson().getFairLock(tempLockName);
        try {
            lock.tryLock(5, lockTime, TimeUnit.SECONDS);
            SpringUtil.getApplicationContext().publishEvent(UnlockEvent.build(lock));
            log.info("==>获取事物锁成功，lockName：" + tempLockName);
        } catch (InterruptedException e) {
            log.error("==>获取事物锁失败，lockName：" + tempLockName);
            throw new RuntimeException("太拥挤了,请重试");
        }
    }

    public static <T extends Serializable> T syncLoad(String lockName, Supplier<T> callBack) {
        return syncLoad(lockName, 10L, callBack);
    }

    public static <T extends Serializable> T syncLoad(String lockName, Long lockTime, Supplier<T> callBack) {
        RLock lock = redisson().getFairLock(keyPrefix + lockName);
        try {
            lock.tryLock(5, lockTime, TimeUnit.SECONDS);
            return callBack.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("太拥挤了,请重试");
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {
            }
        }
    }

    public static void transactionalMultiLock(List<String> lockNames) {
        transactionalMultiLock(lockNames, 10L);
    }

    /**
     * 事务锁,加锁后当事务结束时才释放锁
     */
    public static void transactionalMultiLock(List<String> lockNames, Long lockTime) {
        List<RLock> rLockList = new ArrayList<>();
        for (String lockName : lockNames) {
            RLock lock = redisson().getFairLock(keyPrefix + lockName);
            rLockList.add(lock);
        }
        RedissonMultiLock lock = new RedissonMultiLock(rLockList.toArray(new RLock[]{}));
        try {
            lock.tryLock(5, lockTime, TimeUnit.SECONDS);
            SpringUtil.getApplicationContext().publishEvent(UnlockEvent.build(lock));
        } catch (InterruptedException e) {
            throw new RuntimeException("太拥挤了,请重试");
        }
    }

    public static <T extends Serializable> T syncLoadMultiLock(List<String> lockNames, Supplier<T> callBack) {
        return syncLoadMultiLock(lockNames, 10L, callBack);
    }

    public static <T extends Serializable> T syncLoadMultiLock(List<String> lockNames, Long lockTime, Supplier<T> callBack) {
        List<RLock> rLockList = new ArrayList<>();
        for (String lockName : lockNames) {
            RLock lock = redisson().getFairLock(keyPrefix + lockName);
            rLockList.add(lock);
        }
        RedissonMultiLock lock = new RedissonMultiLock(rLockList.toArray(new RLock[]{}));
        try {
            lock.tryLock(5, lockTime, TimeUnit.SECONDS);
            return callBack.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("太拥挤了,请重试");
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {
            }
        }
    }

    public static <T extends Serializable> T valueGet(String key, Supplier<T> callBack) {
        return valueGet(key, 1, callBack);
    }

    public static <T extends Serializable> T valueGet(String key, int expireTime, Supplier<T> callBack) {
        T model = RedisUtil.<T>valueRedis().get(key);
        if (model != null) {
            return model;
        }
        model = callBack.get();
        if (model != null) {
            if (expireTime == -1) {
                valueRedis().set(key, model);
            } else {
                valueRedis().set(key, model, expireTime, TimeUnit.DAYS);
            }
        }
        return model;
    }

    public static <T extends Serializable> T syncValueGet(String lockName, String key, Supplier<T> callBack) {
        return syncValueGet(lockName, key, 1, callBack);
    }

    public static <T extends Serializable> T syncValueGet(String lockName, String key, int expireTime, Supplier<T> callBack) {
        T model = RedisUtil.<T>valueRedis().get(key);
        if (model != null) {
            return model;
        }
        RLock lock = redisson().getFairLock(keyPrefix + lockName);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            model = RedisUtil.<T>valueRedis().get(key);
            if (model != null) {
                return model;
            }
            model = callBack.get();
            if (model != null) {
                if (expireTime == -1) {
                    valueRedis().set(key, model);
                } else {
                    valueRedis().set(key, model, expireTime, TimeUnit.DAYS);
                }
            }
        } finally {
            try {
                lock.unlock();
            } catch (IllegalMonitorStateException ignored) {
            }
        }
        return model;
    }

}
