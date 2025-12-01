package com.TicketManagementSystem.DamaiTicketing.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // 创建RedisTemplate实例 指定Key为String类型 Value为Object类型
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(redisConnectionFactory);

        // Key的序列化：StringRedisSerializer（保持key的可读性）
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value的序列化：GenericJackson2JsonRedisSerializer（支持所有类型）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;

    }

    // 专门用于 Integer 的 Template
    @Bean
    public RedisTemplate<String, Integer> integerRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());

        // 自定义 Integer 序列化器
        template.setValueSerializer(new RedisSerializer<Integer>() {
            private final Charset charset = StandardCharsets.UTF_8;

            @Override
            public byte[] serialize(Integer integer) {
                return integer == null ? null : integer.toString().getBytes(charset);
            }

            @Override
            public Integer deserialize(byte[] bytes) {
                return bytes == null ? null : Integer.parseInt(new String(bytes, charset));
            }
        });

        template.afterPropertiesSet();
        return template;

    }

}
