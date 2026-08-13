package com.octpus.spring.admin;

import com.octpus.core.model.GatewayResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理控制台 Dashboard API。
 *
 * @author haoli.xu
 * @since 1.4.0
 */
@RestController
@RequestMapping("/octpus/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/stats")
    public GatewayResponse<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();

        // 系统统计
        stats.put("totalSystems", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_system", Integer.class));
        stats.put("onlineSystems", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_system WHERE status = 1", Integer.class));

        // 服务统计
        stats.put("totalServices", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_service", Integer.class));
        stats.put("onlineServices", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM octpus_service WHERE status = 1", Integer.class));

        // 按系统分组的服务数
        List<Map<String, Object>> servicesBySystem = jdbcTemplate.queryForList(
                "SELECT s.system_code, sys.system_name, COUNT(*) as service_count " +
                "FROM octpus_service s JOIN octpus_system sys ON s.system_code = sys.system_code " +
                "GROUP BY s.system_code, sys.system_name ORDER BY service_count DESC");
        stats.put("servicesBySystem", servicesBySystem);

        return GatewayResponse.success(stats);
    }
}
