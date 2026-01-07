package com.TicketManagementSystem.DamaiTicketing.Service;

import com.TicketManagementSystem.DamaiTicketing.Util.HttpRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerformLocationService {

    // 还是感谢 AI

    GeoIPService geoIPService;
    HttpRequestUtils httpRequestUtils;

    @Autowired
    public PerformLocationService(GeoIPService geoIPService, HttpRequestUtils httpRequestUtils) {
        this.geoIPService = geoIPService;
        this.httpRequestUtils = httpRequestUtils;
    }

    // 根据 IP返回演出
    public String returnPerformanceByIP(HttpServletRequest request) {
        // 1. 获取IP
        String clientIp = httpRequestUtils.getClientIpAddress(request);
        log.info("收到请求，客户端IP: {}", clientIp);

        // 2. 查询城市
        Optional<String> cityOptional = geoIPService.getCityByIp(clientIp);
        String city = cityOptional.orElse("未知");

        log.info("IP定位结果 - IP: {}, 城市: {}", clientIp, city);
        return city;
    }

}
