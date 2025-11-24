package com.TicketManagementSystem.DamaiTicketing.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // 告诉Spring:这是一个配置类
public class SwaggerConfig {

    @Bean  // 告诉Spring:请管理我返回的这个对象
    public OpenAPI createOpenAPIConfig() {

        // 创建联系人信息对象
        Contact contactInfo = new Contact();
        contactInfo.setName("开发者");           // 设置联系人姓名
        contactInfo.setEmail("123456@qq.com");     // 设置联系人邮箱

        // 创建API基本信息对象
        Info apiInfo = new Info();
        apiInfo.setTitle("票务管理系统API文档");    // 设置文档标题
        apiInfo.setVersion("1.0");              // 设置版本号
        apiInfo.setDescription("项目API接口文档");  // 设置描述
        apiInfo.setContact(contactInfo);        // 设置联系信息

        // 创建最终的Swagger配置对象并返回
        OpenAPI swaggerConfig = new OpenAPI();
        swaggerConfig.setInfo(apiInfo);         // 把基本信息添加到配置中

        return swaggerConfig;  // 返回配置对象
    }
}