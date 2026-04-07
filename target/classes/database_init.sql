-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    status INT NOT NULL DEFAULT 1,
    student_id VARCHAR(50),
    teacher_id VARCHAR(50),
    department VARCHAR(100),
    last_login_time DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入默认教师账号 (密码: teacher123)
INSERT INTO sys_user (username, password, real_name, role, status, teacher_id, department, created_at, updated_at) VALUES
('teacher', 'e10adc3949ba59abbe56e057f20f883e', '张老师', 'TEACHER', 1, 'T001', '计算机学院', NOW(), NOW());

-- 插入默认学生账号 (密码: student123)
INSERT INTO sys_user (username, password, real_name, role, status, student_id, department, created_at, updated_at) VALUES
('student', 'e10adc3949ba59abbe56e057f20f883e', '李同学', 'STUDENT', 1, 'S001', '计算机学院', NOW(), NOW());

-- 学生表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    courses TEXT,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入学生数据
INSERT INTO student (student_id, name, courses, created_at) VALUES
('S001', '李同学', 'CS101,CS102', NOW()),
('S002', '王同学', 'CS101,CS103', NOW());

-- 智能体表
CREATE TABLE IF NOT EXISTS agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    course_id VARCHAR(50) NOT NULL,
    description TEXT,
    config TEXT,
    avatar VARCHAR(255),
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入智能体数据
INSERT INTO agent (name, course_id, description, config, created_at) VALUES
('数学助教', 'CS101', '帮助学生解答数学相关问题', '{"model": "glm-4-flash", "temperature": 0.7}', NOW()),
('编程导师', 'CS102', '帮助学生学习编程知识', '{"model": "glm-4-flash", "temperature": 0.7}', NOW());

-- 作业表
CREATE TABLE IF NOT EXISTS assignment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    assignment_id VARCHAR(50) NOT NULL UNIQUE,
    course_id VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    deadline DATETIME,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入作业数据
INSERT INTO assignment (assignment_id, course_id, title, content, deadline, created_at) VALUES
('A001', 'CS101', '数学作业1', '完成课本第三章习题', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
('A002', 'CS102', '编程作业1', '编写一个简单的计算器', DATE_ADD(NOW(), INTERVAL 5 DAY), NOW());

-- 作业提交表
CREATE TABLE IF NOT EXISTS submission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id VARCHAR(50) NOT NULL UNIQUE,
    assignment_id VARCHAR(50) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    content TEXT,
    score DECIMAL(5,2),
    feedback TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    submitted_at DATETIME,
    graded_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入作业提交数据
INSERT INTO submission (submission_id, assignment_id, student_id, content, score, feedback, status, submitted_at, graded_at) VALUES
('SUB001', 'A001', 'S001', '已完成所有习题', 85.5, '作业完成质量良好，解题思路清晰', 'graded', NOW(), NOW()),
('SUB002', 'A002', 'S001', '已编写计算器代码', NULL, NULL, 'pending', NOW(), NULL);

-- 知识节点表
CREATE TABLE IF NOT EXISTS knowledge_node (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    node_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    course_id VARCHAR(50) NOT NULL,
    parent_id VARCHAR(50),
    description TEXT,
    difficulty INT DEFAULT 1,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入知识节点数据
INSERT INTO knowledge_node (node_id, name, course_id, parent_id, description, difficulty, created_at) VALUES
('KN001', '高等数学', 'CS101', NULL, '高等数学基础', 3, NOW()),
('KN002', '编程基础', 'CS102', NULL, '编程入门知识', 2, NOW());

-- 学习活动表
CREATE TABLE IF NOT EXISTS learning_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    course_id VARCHAR(50),
    activity_type VARCHAR(50),
    activity_name VARCHAR(200),
    duration INT,
    score DECIMAL(5,2),
    details TEXT,
    timestamp DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入学习活动数据
INSERT INTO learning_activity (student_id, course_id, activity_type, activity_name, duration, score, details, timestamp) VALUES
('S001', 'CS101', '作业', '数学作业1', 120, 85.5, '完成所有习题', NOW()),
('S001', 'CS102', '编程', '编写计算器', 90, NULL, '编写了基本功能', NOW());

-- 学生能力画像表
CREATE TABLE IF NOT EXISTS student_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    knowledge_mastery TEXT,
    learning_style VARCHAR(50),
    strength_areas TEXT,
    weakness_areas TEXT,
    total_study_time INT DEFAULT 0,
    average_score DECIMAL(5,2),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入学生能力画像数据
INSERT INTO student_profile (student_id, knowledge_mastery, learning_style, strength_areas, weakness_areas, total_study_time, average_score, updated_at) VALUES
('S001', '{"CS101": 85, "CS102": 75}', '视觉学习者', '数学分析', '编程实践', 210, 80.25, NOW()),
('S002', '{"CS101": 70, "CS103": 80}', '听觉学习者', '编程逻辑', '数学证明', 180, 75.00, NOW());

-- 精细化批注表
CREATE TABLE IF NOT EXISTS precise_annotation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id VARCHAR(50) NOT NULL,
    start_position INT,
    end_position INT,
    annotation_type VARCHAR(20),
    content TEXT,
    suggestion TEXT,
    created_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入精细化批注数据
INSERT INTO precise_annotation (submission_id, start_position, end_position, annotation_type, content, suggestion, created_at) VALUES
('SUB001', 100, 150, '错误', '计算过程有误', '建议重新检查计算步骤', NOW()),
('SUB001', 200, 250, '改进', '解题方法可以优化', '建议使用更简洁的方法', NOW());

-- 增量练习表
CREATE TABLE IF NOT EXISTS incremental_exercise (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(50) NOT NULL,
    knowledge_node_id VARCHAR(50) NOT NULL,
    difficulty INT,
    content TEXT,
    answer TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    created_at DATETIME NOT NULL,
    completed_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入增量练习数据
INSERT INTO incremental_exercise (student_id, knowledge_node_id, difficulty, content, answer, status, created_at, completed_at) VALUES
('S001', 'KN001', 3, '求解微分方程 y'' + y = 0', '通解为 y = C1*cos(x) + C2*sin(x)', 'pending', NOW(), NULL),
('S001', 'KN002', 2, '编写一个计算阶乘的函数', 'def factorial(n): return 1 if n <= 1 else n * factorial(n-1)', 'pending', NOW(), NULL);
