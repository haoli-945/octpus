-- ============================================================
-- Octpus 网关服务注册表 DDL
-- 适用数据库：MySQL 8.0+ / PostgreSQL 12+
-- ============================================================

-- 1. 系统注册表：维护所有接入系统的访问地址
CREATE TABLE IF NOT EXISTS octpus_system (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    system_code     VARCHAR(64)     NOT NULL COMMENT '系统编码（唯一标识），如 alipay-trade',
    system_name     VARCHAR(128)    NOT NULL COMMENT '系统名称，如 支付宝交易系统',
    base_url        VARCHAR(512)    NOT NULL COMMENT '基础访问地址，如 http://192.168.1.100:8080/service.do',
    weight          INT             DEFAULT 1 COMMENT '负载均衡权重（越大权重越高）',
    status          TINYINT         DEFAULT 1 COMMENT '状态：1=上线, 0=下线',
    description     VARCHAR(256)    DEFAULT NULL COMMENT '系统描述',
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_system_code (system_code)
) COMMENT = '八爪鱼网关 - 系统注册表';

-- 2. 服务接口表：维护 serviceName 与系统的映射关系
CREATE TABLE IF NOT EXISTS octpus_service (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    service_name    VARCHAR(128)    NOT NULL COMMENT '服务名（全局唯一），如 open.alipay.app.query',
    system_code     VARCHAR(64)     NOT NULL COMMENT '所属系统编码（关联 octpus_system.system_code）',
    version         VARCHAR(32)     DEFAULT '1.0' COMMENT '接口版本号',
    description     VARCHAR(256)    DEFAULT NULL COMMENT '接口描述',
    timeout_ms      INT             DEFAULT 3000 COMMENT '调用超时时间（毫秒）',
    retry_count     INT             DEFAULT 0 COMMENT '失败重试次数',
    status          TINYINT         DEFAULT 1 COMMENT '状态：1=上线, 0=下线',
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_time    DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_service_name (service_name),
    KEY idx_system_code (system_code)
) COMMENT = '八爪鱼网关 - 服务接口元数据表';

-- ============================================================
-- 示例数据
-- ============================================================

-- 注册系统
INSERT INTO octpus_system (system_code, system_name, base_url, weight, status, description)
VALUES
    ('alipay-trade', '支付宝交易系统', 'http://192.168.1.100:8080/service.do', 1, 1, '支付宝核心交易服务'),
    ('alipay-user', '支付宝用户系统', 'http://192.168.1.101:8080/service.do', 1, 1, '支付宝用户中心'),
    ('internal-oss', '内部文件系统', 'http://192.168.1.102:8080/service.do', 1, 1, '内部对象存储服务');

-- 注册服务接口
INSERT INTO octpus_service (service_name, system_code, version, description, timeout_ms, status)
VALUES
    ('open.alipay.trade.pay', 'alipay-trade', '1.0', '支付宝当面付-条码支付', 5000, 1),
    ('open.alipay.trade.query', 'alipay-trade', '1.0', '支付宝交易查询', 3000, 1),
    ('open.alipay.trade.close', 'alipay-trade', '1.0', '支付宝交易关闭', 3000, 1),
    ('open.alipay.user.info', 'alipay-user', '1.0', '查询用户信息', 3000, 1),
    ('internal.oss.upload', 'internal-oss', '1.0', '文件上传', 10000, 1);
