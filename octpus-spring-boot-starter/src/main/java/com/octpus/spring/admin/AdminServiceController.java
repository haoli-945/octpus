package com.octpus.spring.admin;

import com.octpus.core.model.GatewayResponse;
import com.octpus.spring.discovery.DatabaseServiceDiscovery;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务管理 REST API - 管理 octpus_service 表。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Slf4j
@RestController
@RequestMapping("/octpus/admin/service")
@RequiredArgsConstructor
public class AdminServiceController {

    private final JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private DatabaseServiceDiscovery serviceDiscovery;

    private final RowMapper<ServiceDTO> serviceRowMapper = (rs, rowNum) -> {
        ServiceDTO dto = new ServiceDTO();
        dto.setId(rs.getLong("id"));
        dto.setServiceName(rs.getString("service_name"));
        dto.setSystemCode(rs.getString("system_code"));
        dto.setVersion(rs.getString("version"));
        dto.setDescription(rs.getString("description"));
        dto.setTimeoutMs(rs.getInt("timeout_ms"));
        dto.setRetryCount(rs.getInt("retry_count"));
        dto.setStatus(rs.getInt("status"));
        return dto;
    };

    /** 查询全部服务（支持按系统编码过滤） */
    @GetMapping("/list")
    public GatewayResponse<List<ServiceDTO>> list(
            @RequestParam(required = false) String systemCode) {
        String sql;
        Object[] args;
        if (systemCode != null && !systemCode.isBlank()) {
            sql = "SELECT * FROM octpus_service WHERE system_code = ? ORDER BY service_name";
            args = new Object[]{systemCode};
        } else {
            sql = "SELECT * FROM octpus_service ORDER BY service_name";
            args = new Object[]{};
        }
        List<ServiceDTO> list = jdbcTemplate.query(sql, serviceRowMapper, args);
        return GatewayResponse.success(list);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public GatewayResponse<ServiceDTO> getById(@PathVariable Long id) {
        ServiceDTO dto = jdbcTemplate.queryForObject(
                "SELECT * FROM octpus_service WHERE id = ?", serviceRowMapper, id);
        return GatewayResponse.success(dto);
    }

    /** 新增服务 */
    @PostMapping
    public GatewayResponse<String> create(@RequestBody ServiceDTO dto) {
        // 检查 serviceName 是否已存在
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_service WHERE service_name = ?",
                Integer.class, dto.getServiceName());
        if (exists != null && exists > 0) {
            return GatewayResponse.fail("SERVICE_NAME_DUPLICATE",
                    "服务名已存在: " + dto.getServiceName());
        }

        // 检查 systemCode 是否存在
        Integer sysExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_system WHERE system_code = ?",
                Integer.class, dto.getSystemCode());
        if (sysExists == null || sysExists == 0) {
            return GatewayResponse.fail("SYSTEM_NOT_FOUND",
                    "系统编码不存在: " + dto.getSystemCode());
        }

        jdbcTemplate.update(
                "INSERT INTO octpus_service (service_name, system_code, version, description, " +
                "timeout_ms, retry_count, status) VALUES (?, ?, ?, ?, ?, ?, ?)",
                dto.getServiceName(), dto.getSystemCode(),
                dto.getVersion() != null ? dto.getVersion() : "1.0",
                dto.getDescription(),
                dto.getTimeoutMs() > 0 ? dto.getTimeoutMs() : 3000,
                dto.getRetryCount(),
                dto.getStatus() != 0 ? 1 : 0
        );
        evictCache(dto.getServiceName());
        log.info("[Octpus Admin] service created: {}", dto.getServiceName());
        return GatewayResponse.success("created");
    }

    /** 修改服务 */
    @PutMapping("/{id}")
    public GatewayResponse<String> update(@PathVariable Long id, @RequestBody ServiceDTO dto) {
        jdbcTemplate.update(
                "UPDATE octpus_service SET service_name=?, system_code=?, version=?, " +
                "description=?, timeout_ms=?, retry_count=?, status=? WHERE id=?",
                dto.getServiceName(), dto.getSystemCode(), dto.getVersion(),
                dto.getDescription(), dto.getTimeoutMs(), dto.getRetryCount(),
                dto.getStatus(), id
        );
        evictCache(dto.getServiceName());
        log.info("[Octpus Admin] service updated: id={}", id);
        return GatewayResponse.success("updated");
    }

    /** 删除服务 */
    @DeleteMapping("/{id}")
    public GatewayResponse<String> delete(@PathVariable Long id) {
        // 先查出 serviceName 用于清缓存
        String serviceName = jdbcTemplate.queryForObject(
                "SELECT service_name FROM octpus_service WHERE id = ?", String.class, id);
        jdbcTemplate.update("DELETE FROM octpus_service WHERE id = ?", id);
        evictCache(serviceName);
        log.info("[Octpus Admin] service deleted: id={}, name={}", id, serviceName);
        return GatewayResponse.success("deleted");
    }

    /** 切换状态（上线/下线） */
    @PatchMapping("/{id}/status")
    public GatewayResponse<String> toggleStatus(@PathVariable Long id) {
        String serviceName = jdbcTemplate.queryForObject(
                "SELECT service_name FROM octpus_service WHERE id = ?", String.class, id);
        jdbcTemplate.update(
                "UPDATE octpus_service SET status = CASE WHEN status=1 THEN 0 ELSE 1 END WHERE id = ?", id);
        evictCache(serviceName);
        log.info("[Octpus Admin] service status toggled: id={}", id);
        return GatewayResponse.success("toggled");
    }

    /** 刷新全部缓存 */
    @PostMapping("/cache/flush")
    public GatewayResponse<String> flushCache() {
        if (serviceDiscovery != null) {
            serviceDiscovery.evictAllCache();
        }
        log.info("[Octpus Admin] all service cache flushed");
        return GatewayResponse.success("cache flushed");
    }

    private void evictCache(String serviceName) {
        if (serviceDiscovery != null && serviceName != null) {
            serviceDiscovery.evictCache(serviceName);
        }
    }

    @Data
    public static class ServiceDTO {
        private Long id;
        private String serviceName;
        private String systemCode;
        private String version;
        private String description;
        private int timeoutMs;
        private int retryCount;
        private int status;
    }
}
