package com.TicketManagementSystem.DamaiTicketing.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AlipayService {
    // PS:以下内容多来自于AI 我只删改了部分 做了一下简化和归类

    @Autowired
    AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    // 解密支付宝公钥
    private PublicKey getPublicKey(String publicKey) throws Exception {

        // Base64解码
        byte[] keyBytes = Base64.getDecoder().decode(publicKey);
        // 创建X509格式的密钥规范
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        // 使用 RSA算法工厂 生成PublicKey对象
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);

    }

    // 验证支付宝回调签名
    public boolean verifySignature(Map<String, String> params) {
        try {
            // 1. 排除sign和sign_type参数
            Map<String, String> paramsToVerify = new HashMap<>(params);
            paramsToVerify.remove("sign");
            paramsToVerify.remove("sign_type");

            // 2. 构建待签名字符串（按支付宝要求格式）
            String content = buildSignContent(paramsToVerify);
            String sign = params.get("sign");

            // 3. 使用支付宝公钥验证签名
            PublicKey publicKey = getPublicKey(alipayPublicKey);
            Signature signature = Signature.getInstance("SHA256WithRSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));

            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            log.error("验证支付宝签名异常", e);
            return false;
        }
    }

    // 构建待签名字符串（按字典序排序）
    private String buildSignContent(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    // 创建支付宝支付订单
    public AlipayTradePagePayResponse createAlipayTrade(String paymentOrderNo, BigDecimal amount, String subject) {
        try {
            AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();

            // 设置回调地址
            request.setNotifyUrl(notifyUrl);

            // 业务参数
            // 感谢AI赞助
            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(paymentOrderNo);
            model.setTotalAmount(amount.toString());
            model.setSubject(subject);
            model.setTimeoutExpress("30m");
            model.setProductCode("FAST_INSTANT_TRADE_PAY");  // 固定值 电脑网站支付

            request.setBizModel(model);

            // 调用支付宝API
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request, "GET");

            if (response.isSuccess()) {
                log.info("支付宝订单创建成功，支付宝订单编号：{}", response.getTradeNo());
                return response;
            } else {
                log.error("支付宝创建订单失败: {}", response.getMsg());
                throw new RuntimeException("支付宝创建订单失败");
            }
        } catch (Exception e) {
            log.error("调用支付宝接口异常", e);
            throw new RuntimeException("支付宝服务异常");
        }
    }

    // 关闭支付宝订单（未支付状态订单）
    public boolean closeAlipayTrade(String paymentOrderNo) {
        try {
            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();

            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(paymentOrderNo);
            request.setBizModel(model);

            AlipayTradeCloseResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                log.info("交易订单关闭成功: {}", paymentOrderNo);
                return true;
            } else {
                log.error("关闭支付宝订单失败: {}", response.getMsg());
                return false;
            }
        } catch (Exception e) {
            log.error("关闭支付宝订单异常", e);
            return false;
        }
    }

    // 查询支付宝订单状态
    public AlipayTradeQueryResponse queryAlipayOrderStatus(String outTradeNo) {
        try {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();

            // 构建请求参数
            Map<String, Object> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", outTradeNo);

            // 将 Map 转换为JSON字符串
            String bizContentJson = convertToJson(bizContent);
            request.setBizContent(bizContentJson);

            log.debug("查询支付宝订单，商户订单号: {}", outTradeNo);

            // 执行查询
            return alipayClient.execute(request);

        } catch (AlipayApiException e) {
            log.error("支付宝查询接口异常，订单: {}，错误码: {}，错误信息: {}",
                    outTradeNo, e.getErrCode(), e.getErrMsg(), e);
            return null;
        } catch (Exception e) {
            log.error("查询支付宝订单未知异常，订单: {}", outTradeNo, e);
            return null;
        }
    }

    // Map转JSON（简化实现）
    // 再次感谢AI
    public String convertToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\"");
            } else {
                json.append(entry.getValue());
            }
            json.append(",");
        }
        if (json.charAt(json.length() - 1) == ',') {
            json.deleteCharAt(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }

}
