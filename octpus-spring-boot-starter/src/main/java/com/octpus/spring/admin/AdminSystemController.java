package com.octpus.spring.admin;

import com.octpus.core.model.GatewayResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统管理 REST API - 管理 octpus_system 表。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@Slf4j
@RestController
@RequestMapping("/octpus/admin/system")
@RequiredArgsConstructor
public class AdminSystemController {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SystemDTO> systemRowMapper = (rs, rowNum) -> {
        SystemDTO dto = new SystemDTO();
        dto.setId(rs.getLong("id"));
        dto.setSystemCode(rs.getString("system_code"));
        dto.setSystemName(rs.getString("system_name"));
        dto.setBaseUrl(rs.getString("base_url"));
        dto.setWeight(rs.getInt("weight"));
        dto.setStatus(rs.getInt("status"));
        dto.setDescription(rs.getString("description"));
        return dto;
    };

    /** 查询全部系统 */
    @GetMapping("/list")
    public GatewayResponse<List<SystemDTO>> list() {
        List<SystemDTO> list = jdbcTemplate.query(
                "SELECT * FROM octpus_system ORDER BY id", systemRowMapper);
        return GatewayResponse.success(list);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public GatewayResponse<SystemDTO> getById(@PathVariable Long id) {
        SystemDTO dto = jdbcTemplate.queryForObject(
                "SELECT * FROM octpus_system WHERE id = ?", systemRowMapper, id);
        return GatewayResponse.success(dto);
    }

    /** 新增系统 */
    @PostMapping
    public GatewayResponse<String> create(@RequestBody SystemDTO dto) {
        jdbcTemplate.update(
                "INSERT INTO octpus_system (system_code, system_name, base_url, weight, status, description) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                dto.getSystemCode(), dto.getSystemName(), dto.getBaseUrl(),
                dto.getWeight() > 0 ? dto.getWeight() : 1,
                dto.getStatus() != 0 ? 1 : 0,
                dto.getDescription()
        );
        log.info("[Octpus Admin] system created: {}", dto.getSystemCode());
        return GatewayResponse.success("created");
    }

    /** 修改系统 */
    @PutMapping("/{id}")
    public GatewayResponse<String> update(@PathVariable Long id, @RequestBody SystemDTO dto) {
        jdbcTemplate.update(
                "UPDATE octpus_system SET system_code=?, system_name=?, base_url=?, " +
                "weight=?, status=?, description=? WHERE id=?",
                dto.getSystemCode(), dto.getSystemName(), dto.getBaseUrl(),
                dto.getWeight(), dto.getStatus(), dto.getDescription(), id
        );
        log.info("[Octpus Admin] system updated: id={}", id);
        return GatewayResponse.success("updated");
    }

    /** 删除系统 */
    @DeleteMapping("/{id}")
    public GatewayResponse<String> delete(@PathVariable Long id) {
        // 检查是否有关联的服务
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_service WHERE system_code = " +
                "(SELECT system_code FROM octpus_system WHERE id = ?)", Integer.class, id);
        if (count != null && count > 0) {
            return GatewayResponse.fail("SYSTEM_HAS_SERVICES",
                    "该系统下还有 " + count + " 个服务，请先删除服务");
        }
        jdbcTemplate.update("DELETE FROM octpus_system WHERE id = ?", id);
        log.info("[Octpus Admin] system deleted: id={}", id);
        return GatewayResponse.success("deleted");
    }

    /** 切换状态（上线/下线） */
    @PatchMapping("/{id}/status")
    public GatewayResponse<String> toggleStatus(@PathVariable Long id) {
        jdbcTemplate.update(
                "UPDATE octpus_system SET status = CASE WHEN status=1 THEN 0 ELSE 1 END WHERE id = ?", id);
        log.info("[Octpus Admin] system status toggled: id={}", id);
        return GatewayResponse.success("toggled");
    }

    @Data
    public static class SystemDTO {
        private Long id;
        private String systemCode;
        private String systemName;
        private String baseUrl;
        private int weight;
        private int status;
        private String description;
    }
}
