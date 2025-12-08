package com.TicketManagementSystem.DamaiTicketing.Config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        paginationInterceptor.setDbType(DbType.MYSQL); // 设置数据库类型
        paginationInterceptor.setOverflow(true); // 超过最大页数后是否回到第一页
        paginationInterceptor.setMaxLimit(1000L); // 单页分页条数限制

        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}
