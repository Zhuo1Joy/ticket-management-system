package com.TicketManagementSystem.DamaiTicketing.Util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class HttpRequestUtils {

    // 写在开头 这里面的代码百分百来源于AI 我唯一的改动就是换了个能通的IP 嘻嘻

    /**
     * 从 HttpServletRequest 中提取客户端真实 IP
     * 处理了常见的代理头（如 Nginx, Cloudflare, AWS 等设置的）
     */
    public String getClientIpAddress(HttpServletRequest request) {
        // 定义可能的代理 IP 头 按优先级检查
        String[] ipHeaders = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        String ip = null;
        for (String header : ipHeaders) {
            ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能包含逗号分隔的 IP 链 这里取第一个
                if (header.equals("X-Forwarded-For")) {
                    int index = ip.indexOf(',');
                    if (index != -1) {
                        ip = ip.substring(0, index);
                    }
                }
                break;
            }
        }

        // 如果以上头部都没有 则使用远程地址
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 本地测试时 将 localhost 映射为测试 IP
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            // 这里可以配置一个默认的测试IP 例如北京
            ip = "8.8.8.8"; // 仅供测试 应通过配置读取
            log.debug("本地访问，使用测试 IP: {}", ip);
        }

        return ip.trim();
    }
}
