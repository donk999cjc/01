
-- ============================================
-- 第1部分：为 assignment 表添加新字段
-- ============================================

-- 1.1 添加 description 字段（作业要求描述）
ALTER TABLE assignment ADD COLUMN  description TEXT DEFAULT NULL COMMENT '作业要求描述' AFTER content;

-- 1.2 添加 total_score 字段（总分）
ALTER TABLE assignment ADD COLUMN   total_score INT DEFAULT 100 COMMENT '总分' AFTER description;

-- 1.3 添加 status 字段（状态）
ALTER TABLE assignment ADD COLUMN  status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/published/closed' AFTER total_score;

-- 1.4 添加 due_date 字段（截止时间）- 这是关键！
ALTER TABLE assignment ADD COLUMN  due_date DATETIME DEFAULT NULL COMMENT '截止时间' AFTER status;

-- 1.5 作业扩展字段（：类型、评分标准、迟交规则、附件）
ALTER TABLE assignment ADD COLUMN  homework_type VARCHAR(20) DEFAULT 'WRITTEN' COMMENT '作业类型' AFTER due_date;
ALTER TABLE assignment ADD COLUMN  grading_standard TEXT COMMENT '评分标准(AI批改)' AFTER homework_type;
ALTER TABLE assignment ADD COLUMN  allow_late TINYINT(1) DEFAULT 0 COMMENT '是否允许迟交' AFTER grading_standard;
ALTER TABLE assignment ADD COLUMN  late_penalty INT DEFAULT 10 COMMENT '每日扣分比例' AFTER allow_late;
ALTER TABLE assignment ADD COLUMN  attachment_url VARCHAR(255) COMMENT '作业附件地址' AFTER late_penalty;
-- ====================================================================

-- 1.6 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'assignment'
  AND COLUMN_NAME IN ('description', 'total_score', 'status', 'due_date','homework_type','grading_standard','allow_late','late_penalty','attachment_url')
ORDER BY ORDINAL_POSITION;

-- ============================================
-- 第2部分：更新现有数据（将deadline同步到due_date）
-- ============================================

-- 如果有旧的 deadline 数据，复制到 due_date
UPDATE assignment SET due_date = deadline WHERE due_date IS NULL AND deadline IS NOT NULL;

-- ============================================
-- 第3部分：为 submission 表添加新字段（如果还没有）
-- ============================================

-- 3.1 添加 file_path 字段
ALTER TABLE submission ADD COLUMN  file_path VARCHAR(500) DEFAULT NULL COMMENT '图片文件存储路径' AFTER content;

-- 3.2 添加 evaluation_report 字段
ALTER TABLE submission ADD COLUMN  evaluation_report TEXT DEFAULT NULL COMMENT 'AI评价报告（JSON格式）' AFTER image_analysis;

-- 3.3 添加 learning_summary 字段
ALTER TABLE submission ADD COLUMN  learning_summary TEXT DEFAULT NULL COMMENT '学习建议总结' AFTER evaluation_report;

-- ============================================
-- 第4部分：验证和查看结果
-- ============================================

-- 4.1 查看 assignment 表结构
SHOW COLUMNS FROM assignment;

-- 4.2 查看现有作业数据
SELECT id, title, course_id, description, total_score, status, due_date, homework_type, allow_late, late_penalty
FROM assignment
ORDER BY created_at DESC
    LIMIT 10;

-- 4.3 查看 submission 表结构
SHOW COLUMNS FROM submission;

-- ============================================

