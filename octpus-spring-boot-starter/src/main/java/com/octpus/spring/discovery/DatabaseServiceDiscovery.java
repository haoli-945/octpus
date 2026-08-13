package com.octpus.spring.discovery;

import com.octpus.core.discovery.ServiceDiscovery;
import com.octpus.core.discovery.ServiceEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 基于数据库的服务发现实现。
 * <p>
 * 通过两张表完成服务路由：
 * <ul>
 *   <li>octpus_service：服务接口元数据（serviceName → systemCode）</li>
 *   <li>octpus_system：系统注册信息（systemCode → URL/权重/状态）</li>
 * </ul>
 * <p>
 * 内置本地缓存（默认 60s TTL），避免每次请求都查库。
 * 缓存失效策略：写时失效（服务上下线时主动清除缓存）。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Slf4j
public class DatabaseServiceDiscovery implements ServiceDiscovery {

    private final JdbcTemplate jdbcTemplate;
    private final long cacheTtlMs;

    /** 本地缓存：serviceName → ServiceEndpoint */
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public DatabaseServiceDiscovery(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, 60_000L);
    }

    public DatabaseServiceDiscovery(JdbcTemplate jdbcTemplate, long cacheTtlMs) {
        this.jdbcTemplate = jdbcTemplate;
        this.cacheTtlMs = cacheTtlMs;
    }

    @Override
    public ServiceEndpoint resolve(String serviceName, String version) {
        // 1. 查缓存
        CacheEntry cached = cache.get(serviceName);
        if (cached != null && !cached.isExpired()) {
            return cached.endpoint;
        }

        // 2. 查数据库（两张表 JOIN）
        ServiceEndpoint endpoint = queryFromDatabase(serviceName, version);

        // 3. 写缓存
        if (endpoint != null) {
            cache.put(serviceName, new CacheEntry(endpoint));
            log.debug("[Octpus] cached endpoint: {} -> {}", serviceName, endpoint.getUrl());
        } else {
            // 缓存 negative result 短时间，防止缓存穿透
            cache.put(serviceName, new CacheEntry(null, 10_000L));
            log.debug("[Octpus] no endpoint found for: {}", serviceName);
        }

        return endpoint;
    }

    /**
     * 主动清除缓存（服务上下线时调用）。
     */
    public void evictCache(String serviceName) {
        cache.remove(serviceName);
        log.info("[Octpus] cache evicted: {}", serviceName);
    }

    /**
     * 清除全部缓存。
     */
    public void evictAllCache() {
        cache.clear();
        log.info("[Octpus] all cache evicted");
    }

    private ServiceEndpoint queryFromDatabase(String serviceName, String version) {
        String sql = """
                SELECT s.service_name, s.version, s.timeout_ms,
                       sys.system_code, sys.system_name, sys.base_url, sys.weight, sys.status
                FROM octpus_service s
                JOIN octpus_system sys ON s.system_code = sys.system_code
                WHERE s.service_name = ?
                  AND s.status = 1
                  AND sys.status = 1
                """;

        // 如果指定了版本，追加版本过滤
        if (version != null && !version.isBlank()) {
            sql += " AND s.version = ?";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    ServiceEndpoint.builder()
                            .url(rs.getString("base_url"))
                            .weight(rs.getInt("weight"))
                            .timeoutMs(rs.getInt("timeout_ms"))
                            .systemCode(rs.getString("system_code"))
                            .systemName(rs.getString("system_name"))
                            .build(),
                    serviceName, version
            );
        } else {
            // 未指定版本，取默认版本（按 version 字段排序取第一个）
            sql += " ORDER BY s.version ASC LIMIT 1";
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    ServiceEndpoint.builder()
                            .url(rs.getString("base_url"))
                            .weight(rs.getInt("weight"))
                            .timeoutMs(rs.getInt("timeout_ms"))
                            .systemCode(rs.getString("system_code"))
                            .systemName(rs.getString("system_name"))
                            .build(),
                    serviceName
            );
        }
    }

    // ==================== 内部缓存结构 ====================

    private static class CacheEntry {
        final ServiceEndpoint endpoint;
        final long expireAt;

        CacheEntry(ServiceEndpoint endpoint) {
            this(endpoint, 60_000L);
        }

        CacheEntry(ServiceEndpoint endpoint, long ttlMs) {
            this.endpoint = endpoint;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }
}
