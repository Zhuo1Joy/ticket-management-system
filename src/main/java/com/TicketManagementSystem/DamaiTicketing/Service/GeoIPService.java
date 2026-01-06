package com.TicketManagementSystem.DamaiTicketing.Service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Subdivision;
import com.maxmind.db.Reader;
import com.maxmind.db.Metadata;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class GeoIPService {

    // 同样感谢 AI支持

    private DatabaseReader databaseReader;

    // 初始化 DatabaseReader
    @PostConstruct
    public void initDatabaseReader() {
        try {
            File database = new ClassPathResource("geoip/GeoLite2-City.mmdb").getFile();
            databaseReader = new DatabaseReader.Builder(database).build();

            try (Reader fileReader = new Reader(database)) {
                Metadata metadata = fileReader.getMetadata();
                // 使用 Record风格的字段访问方法
                log.info("GeoIP2 数据库加载成功。类型: {}, 构建时间: {}",
                        metadata.databaseType(),
                        metadata.buildTime());
            }

        } catch (IOException e) {
            log.error("无法加载 GeoIP2 数据库文件", e);
            throw new RuntimeException("初始化 IP 定位服务失败", e);
        }
    }

    // 根据 IP 地址查询城市信息（核心方法）
    public Optional<String> getCityByIp(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            log.info("IP 地址为空");
            return Optional.empty();
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse response = databaseReader.city(inetAddress);

            // 1. 获取城市Record和其本地化名称映射
            City city = response.city();
            Map<String, String> cityNames = (city != null) ? city.names() : null;
            String cityName = null;

            // 2. 优先尝试获取中文城市名
            if (cityNames != null) {
                cityName = cityNames.get("zh-CN");
            }
            // 3. 若无中文名 则使用英文名
            if (cityName == null || cityName.isEmpty()) {
                cityName = (city != null) ? city.name() : null; // 使用Record的name()方法
            }
            // 4. 若城市名为空 则回退到主要的行政区划（例如省、州）
            if (cityName == null || cityName.isEmpty()) {
                List<Subdivision> subdivisions = response.subdivisions(); // 使用新方法获取列表
                if (subdivisions != null && !subdivisions.isEmpty()) {
                    // 获取第一个（通常是最具体的）行政区划
                    Subdivision subdivision = subdivisions.getFirst();
                    Map<String, String> subNames = subdivision.names();
                    if (subNames != null) {
                        cityName = subNames.get("zh-CN");
                    }
                    if (cityName == null || cityName.isEmpty()) {
                        cityName = subdivision.name(); // 使用Record的name()方法
                    }
                }
            }
            log.info("IP 地址解析成功：{}", cityName);
            return Optional.ofNullable(cityName);
        } catch (IOException e) {
            log.warn("IP 地址解析失败: {}", ipAddress, e);
        } catch (GeoIp2Exception e) {
            log.debug("IP 地址未在数据库中找到: {}", ipAddress);
        }
        return Optional.empty();
    }

    // 销毁时关闭资源
    @PreDestroy
    public void destroyDatabaseReader() {
        if (databaseReader != null) {
            try {
                databaseReader.close();
                log.info("GeoIP2 数据库资源已关闭");
            } catch (IOException e) {
                log.error("关闭 GeoIP2 数据库资源时出错", e);
            }
        }
    }
}