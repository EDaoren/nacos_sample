package com.edaoren.intercept;

import com.edaoren.api.annotation.Limiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * 限流拦截器
 *
 * @author EDaoren
 */
@Slf4j
@Aspect
@Component
public class LimiterInterceptor {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript redisRequestRateLimiterScript;

    private final HttpServletRequest httpServletRequest;

    public LimiterInterceptor(StringRedisTemplate redisTemplate, RedisScript redisRequestRateLimiterScript, HttpServletRequest httpServletRequest) {
        this.redisTemplate = redisTemplate;
        this.redisRequestRateLimiterScript = redisRequestRateLimiterScript;
        this.httpServletRequest = httpServletRequest;
    }



    @Pointcut("execution(public * *(..)) && @annotation(com.edaoren.api.annotation.Limiter)")
    public void pointCut() {

    }



    /**
     * 前置通知
     * @param joinPoint
     */
    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint){

        log.info("***********前置通知**************");
    }

    /**
     * 后置通知
     * @param joinPoint
     */
    @After("pointCut()")
    public  void after(JoinPoint joinPoint) throws Exception{
        log.info("***********后置通知**************");
    }


    /**
     * 对注解的处理
     *
     * @param point
     * @return
     * @throws Throwable 参考Spring Gateway
     *                   org.springframework.cloud.gateway.filter.ratelimit.isAllowed()
     *                   redis + lua脚本令牌桶算法限流
     */
    @Around(value = "pointCut()")
    public Object interceptor(ProceedingJoinPoint point) throws Throwable {
        log.info("rate limiter");
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Limiter limiter = method.getAnnotation(Limiter.class);
        String key = "";
        switch (limiter.type()) {
            case PATH:
                int hashCode = signature.toLongString().hashCode();
                key = hashCode + "";
                log.info("limiter by path, path hash code ->{}", hashCode);
                break;
            case IP:
                key = getIpAddress();
                log.info("limiter by ip, ip -> {}", key);
                break;
            case USER:
                key = getUserId();
                log.info("limiter by userId, userId -> {}", key);
                break;
            default:
                break;
        }

        try {
            List<String> keys = getKeys(key);
            String[] args = new String[]{limiter.replenishRate() + "", limiter.burstCapacity() + "", Instant.now().getEpochSecond() + "", limiter.requestedTokens() + ""};
            List<Long> results = (List<Long>) this.redisTemplate.execute(redisRequestRateLimiterScript, keys, args);
            assert results != null;
            boolean allowed = results.get(0) == 1L;
            if (!allowed) {
                return "Too Many Requests!";
            }
            Long tokensLeft = results.get(1);
            log.info("tokens left ->{}", tokensLeft);
        } catch (Exception e) {
            /*
             * We don't want a hard dependency on Redis to allow traffic. Make sure to set
             * an alert so you know if this is happening too much. Stripe's observed
             * failure rate is 0.01%.
             */
            log.error("Error determining if user allowed from redis", e);
        }
        return point.proceed();
    }

    static List<String> getKeys(String key) {
        // use `{}` around keys to use Redis Key hash tags
        // this allows for using redis cluster

        // Make a unique key per user.
        String prefix = "request_rate_limiter.{" + key;

        // You need two Redis keys for Token Bucket.
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    public String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public String getUserId() {
        String userId = httpServletRequest.getHeader("customer-id");
        if (StringUtils.hasText(userId)) {
            return userId;
        }
        return "unknown";
    }
}
