-- ============================================
-- BKT 学生掌握度模型 + Redis 缓存 + 工作流 数据库迁移
-- ============================================

USE agent_framework;

-- ============================================
-- 第1部分：创建 student_mastery 表
-- ============================================

CREATE TABLE IF NOT EXISTS student_mastery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(50) NOT NULL COMMENT '学生ID',
    knowledge_id VARCHAR(100) NOT NULL COMMENT '知识点ID',
    course_id VARCHAR(50) DEFAULT '' COMMENT '课程ID',
    p_l DOUBLE DEFAULT 0.3 COMMENT '当前掌握概率 P(L)',
    p_l0 DOUBLE DEFAULT 0.3 COMMENT '初始掌握概率 P(L0)',
    p_t DOUBLE DEFAULT 0.1 COMMENT '学习转移概率 P(T)',
    p_g DOUBLE DEFAULT 0.2 COMMENT '猜测概率 P(G)',
    p_s DOUBLE DEFAULT 0.1 COMMENT '失误概率 P(S)',
    total_attempts INT DEFAULT 0 COMMENT '总尝试次数',
    correct_attempts INT DEFAULT 0 COMMENT '正确次数',
    last_attempt_at DATETIME DEFAULT NULL COMMENT '最近尝试时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_knowledge (student_id, knowledge_id),
    KEY idx_student_id (student_id),
    KEY idx_course_id (course_id),
    KEY idx_knowledge_id (knowledge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BKT学生知识点掌握度';

-- ============================================
-- 第2部分：为 knowledge_node 表添加向量索引字段
-- ============================================

ALTER TABLE knowledge_node ADD COLUMN  embedding_id VARCHAR(100) DEFAULT NULL COMMENT '向量索引ID' AFTER difficulty;
ALTER TABLE knowledge_node ADD COLUMN  chunk_count INT DEFAULT 0 COMMENT '分块数量' AFTER embedding_id;
ALTER TABLE knowledge_node ADD COLUMN  tags VARCHAR(500) DEFAULT NULL COMMENT '标签(逗号分隔)' AFTER chunk_count;

-- ============================================
-- 第3部分：创建 workflow_execution 表
-- ============================================

CREATE TABLE IF NOT EXISTS workflow_execution (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_id VARCHAR(100) NOT NULL COMMENT '执行ID',
    workflow_id VARCHAR(100) NOT NULL COMMENT '工作流ID',
    workflow_name VARCHAR(200) DEFAULT NULL COMMENT '工作流名称',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态',
    context TEXT DEFAULT NULL COMMENT '执行上下文(JSON)',
    result TEXT DEFAULT NULL COMMENT '执行结果(JSON)',
    step_count INT DEFAULT 0 COMMENT '步骤数',
    error TEXT DEFAULT NULL COMMENT '错误信息',
    started_at DATETIME DEFAULT NULL COMMENT '开始时间',
    completed_at DATETIME DEFAULT NULL COMMENT '完成时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_execution_id (execution_id),
    KEY idx_workflow_id (workflow_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行记录';

-- ============================================
-- 第4部分：验证
-- ============================================

SHOW TABLES;
DESCRIBE student_mastery;
DESCRIBE workflow_execution;
