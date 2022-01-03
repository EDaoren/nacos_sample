package com.edaoren.config;

import com.alibaba.fastjson.JSON;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.nio.charset.Charset;

/**
 * @author EDaoren
 */
public class CustomizeStringRedisSerializer implements RedisSerializer<Object> {
    private final Charset charset;
    private final String target = "\"";
    private final String replacement = "";

    public CustomizeStringRedisSerializer() {
        this(Charset.forName("UTF8"));
    }

    public CustomizeStringRedisSerializer(Charset charset) {
        Assert.notNull(charset, "Charset must not be null!");
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        return (bytes == null ? null : new String(bytes, charset));
    }

    @Override
    public byte[] serialize(Object object) {
        //底层还是调用的fastjson的工具来操作的
        String string = JSON.toJSONString(object);
        if (string == null) {
            return null;
        }
        string = string.replace(target, replacement);
        return string.getBytes(charset);
    }
}
