-- ============================================
-- 🚨 重要：完整数据库迁移脚本（必须执行）
-- 执行时间：2026-04-10
-- 说明：此脚本会添加作业表的新字段，并清理旧数据
-- ============================================

USE test;

-- ============================================
-- 第1部分：为 assignment 表添加新字段
-- ============================================

-- 1.1 添加 description 字段（作业要求描述）
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS description TEXT DEFAULT NULL COMMENT '作业要求描述' AFTER content;

-- 1.2 添加 total_score 字段（总分）
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS total_score INT DEFAULT 100 COMMENT '总分' AFTER description;

-- 1.3 添加 status 字段（状态）
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'draft' COMMENT '状态：draft/published/closed' AFTER total_score;

-- 1.4 添加 due_date 字段（截止时间）- 这是关键！
ALTER TABLE assignment ADD COLUMN IF NOT EXISTS due_date DATETIME DEFAULT NULL COMMENT '截止时间' AFTER status;

-- 1.5 验证字段是否添加成功
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'assignment' 
AND COLUMN_NAME IN ('description', 'total_score', 'status', 'due_date')
ORDER BY ORDINAL_POSITION;

-- ============================================
-- 第2部分：更新现有数据（将deadline同步到due_date）
-- ============================================

-- 如果有旧的 deadline 数据，复制到 due_date
UPDATE assignment SET due_date = deadline WHERE due_date IS NULL AND deadline IS NOT NULL;

-- 如果有截止时间数据但是字符串格式，尝试转换
-- UPDATE assignment SET due_date = STR_TO_DATE(deadline, '%Y-%m-%d %H:%i:%s') WHERE due_date IS NULL AND deadline LIKE '%-%';

-- ============================================
-- 第3部分：为 submission 表添加新字段（如果还没有）
-- ============================================

-- 3.1 添加 file_path 字段
ALTER TABLE submission ADD COLUMN IF NOT EXISTS file_path VARCHAR(500) DEFAULT NULL COMMENT '图片文件存储路径' AFTER content;

-- 3.2 添加 evaluation_report 字段
ALTER TABLE submission ADD COLUMN IF NOT EXISTS evaluation_report TEXT DEFAULT NULL COMMENT 'AI评价报告（JSON格式）' AFTER image_analysis;

-- 3.3 添加 learning_summary 字段
ALTER TABLE submission ADD COLUMN IF NOT EXISTS learning_summary TEXT DEFAULT NULL COMMENT '学习建议总结' AFTER evaluation_report;

-- ============================================
-- 第4部分：验证和查看结果
-- ============================================

-- 4.1 查看 assignment 表结构
SHOW COLUMNS FROM assignment;

-- 4.2 查看现有作业数据（检查是否有due_date）
SELECT id, title, course_id, description, total_score, status, due_date, deadline, created_at 
FROM assignment 
ORDER BY created_at DESC 
LIMIT 10;

-- 4.3 查看 submission 表结构
SHOW COLUMNS FROM submission;

-- ============================================
-- ✅ 完成！
-- ============================================
-- 执行完此脚本后，请重启Spring Boot应用
-- 然后测试以下功能：
-- 1. 新建作业（填写标题、课程ID、截止时间、描述等）
-- 2. 编辑作业（修改截止时间和描述）
-- 3. 查看作业列表（应显示截止时间和描述）
-- ============================================
