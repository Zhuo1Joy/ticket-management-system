package com.TicketManagementSystem.DamaiTicketing.Config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.database:0}")
    private int database;

    @Value("${spring.redis.timeout:3000}")
    private int timeout;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        try {
            Config config = new Config();

            // 构建 Redis地址
            String address = String.format("redis://%s:%d", redisHost, redisPort);

            config.useSingleServer()
                    .setAddress(address)
                    .setDatabase(database)
                    .setTimeout(timeout)
                    .setConnectionPoolSize(64)      // 最大连接数
                    .setConnectionMinimumIdleSize(10) // 最小空闲连接数
                    .setIdleConnectionTimeout(10000)   // 空闲连接超时时间
                    .setConnectTimeout(10000)          // 连接超时时间
                    .setRetryAttempts(3)              // 重试次数
                    .setRetryInterval(1500);          // 重试间隔

            return Redisson.create(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Redisson client", e);
        }
    }

}
