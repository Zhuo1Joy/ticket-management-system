package com.TicketManagementSystem.DamaiTicketing.Config;

import com.alipay.api.AlipayClient;           // 支付宝客户端接口
import com.alipay.api.DefaultAlipayClient;    // 支付宝客户端实现类
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 支付宝客户端配置类
// 初始化AlipayClient
@Configuration
public class AlipayConfig {

    @Bean  // 将方法返回的对象注册为Spring Bean
    public AlipayClient alipayClient(AlipayProperties alipayProperties) {
        // 创建DefaultAlipayClient实例
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),  // 网关地址
                alipayProperties.getAppId(),       // APP_ID
                alipayProperties.getAppPrivateKey(), // 应用私钥
                alipayProperties.getFormat(),      // 数据格式
                alipayProperties.getCharset(),     // 字符编码
                alipayProperties.getAlipayPublicKey(), // 支付宝公钥
                alipayProperties.getSignType()     // 签名类型
        );
    }
}
