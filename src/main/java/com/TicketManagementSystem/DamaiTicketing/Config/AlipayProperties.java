package com.TicketManagementSystem.DamaiTicketing.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性类
 * 作用：读取application.yml中以alipay为前缀的配置
 * 使用@ConfigurationProperties注解自动绑定配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {

    /**
     * 是否使用沙箱环境（测试环境）
     */
    private Boolean sandbox;

    /**
     * 应用ID（APP_ID）
     * 从支付宝开放平台获取
     */
    private String appId;

    /**
     * 应用私钥（APP_PRIVATE_KEY）
     * 用于生成签名，不可泄露
     */
    private String appPrivateKey;

    /**
     * 支付宝公钥（ALIPAY_PUBLIC_KEY）
     * 用于验证支付宝返回的数据签名
     */
    private String alipayPublicKey;

    /**
     * 支付宝网关地址
     */
    private String gatewayUrl;

    /**
     * 异步通知地址（回调地址）
     * 支付成功后支付宝会POST请求这个地址
     */
    private String notifyUrl;

    /**
     * 同步返回地址
     * 支付成功后页面跳转的地址
     */
    private String returnUrl;

    /**
     * 字符编码格式
     */
    private String charset;

    /**
     * 签名算法类型
     * RSA2：推荐使用，更安全
     */
    private String signType;

    /**
     * 数据格式
     * JSON：支付宝API统一使用JSON格式
     */
    private String format;

}
