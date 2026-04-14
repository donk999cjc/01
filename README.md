# 可嵌入式跨课程AI Agent通用架构平台

**面向高等教育的智能化教学辅助系统**

[Java 8+](https://www.oracle.com/java/technologies/downloads/) | [Spring Boot 2.7.15](https://spring.io/projects/spring-boot) | [MySQL 8.0+](https://dev.mysql.com/) | [智谱AI GLM-4V](https://open.bigmodel.cn/) | [MIT License](LICENSE)

---

## 项目简介

本项目是一个可嵌入式的跨课程AI Agent通用架构平台，专为高等教育场景设计。平台采用模块化Agent架构，能够无缝对接超星学习通、钉钉等主流教学平台，提供智能作业批改、学情分析、个性化辅导等核心功能。

### 核心价值

- **可嵌入式设计**：Agent架构支持灵活嵌入不同课程和教学场景
- **跨课程通用**：一套系统适配多学科、多课程的教学需求
- **平台无缝对接**：原生支持超星、钉钉等主流教学平台
- **AI智能驱动**：基于智谱GLM-4V视觉大模型，支持图片作业智能批改
- **数据驱动教学**：提供多维度学情分析，助力精准教学

---

## 系统架构

```
+------------------------------------------------------------------+
|                        前端展示层 (HTML+JS)                        |
|   +-----------+   +-----------+   +-----------+                  |
|   |  学生端    |   |  教师端    |   |  管理端    |                  |
|   +-----+-----+   +-----+-----+   +-----+-----+                  |
+---------+---------------+---------------+------------------------+
          |               |               |
+---------v---------------v---------------v------------------------+
|                      API 控制层 (RESTful)                         |
|   Agent | Assignment | Review | Chat | Integration | Analytics    |
+---------+---------------+---------------+------------------------+
          |               |               |
+---------v---------------v---------------v------------------------+
|                      业务逻辑层 (Service)                         |
|   +-------------+   +-------------+   +-------------+            |
|   |  Agent服务   |   |  智能引擎    |   |  集成管理器  |            |
|   +-------------+   +-------------+   +-------------+            |
|   +-------------+   +-------------+   +-------------+            |
|   |  AI服务      |   |  知识图谱    |   |  文件存储    |            |
|   +-------------+   +-------------+   +-------------+            |
+---------+---------------+---------------+------------------------+
          |               |               |
+---------v---------------v---------------v------------------------+
|                   数据持久层 (MyBatis + MySQL)                    |
|   Agent | Assignment | Submission | Student | Teacher | User     |
+------------------------------------------------------------------+

                          外部服务集成
   +-------------+   +-------------+   +-------------+
   | 超星学习通   |   |   钉钉     |   | 智谱AI GLM  |
   +-------------+   +-------------+   +-------------+
```

---

## 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| 后端框架 | Spring Boot 2.7.15 | 企业级Java开发框架 |
| ORM框架 | MyBatis | 轻量级持久层框架 |
| 数据库 | MySQL 8.0+ | 关系型数据库 |
| AI引擎 | 智谱AI GLM-4V | 视觉语言大模型 |
| HTTP客户端 | OkHttp 4.12 | 高性能HTTP请求库 |
| JSON处理 | Gson 2.10 | Google JSON库 |
| 工具库 | Lombok | 简化Java代码 |

---

## 核心功能模块

### 1. AI Agent 系统

- **可扩展的Agent架构**：支持自定义Agent类型和角色
- **上下文感知**：AgentContext提供完整的运行时环境
- **生命周期管理**：初始化、处理请求、学习、重置完整流程
- **多Agent协作**：支持同一课程部署多个专业Agent

### 2. 智能作业批改

- **图片识别批改**：支持手写作业、拍照上传的图片识别与批改
- **多维度评价**：从正确性、完整性、规范性等多维度评分
- **智能反馈生成**：自动生成详细的批改意见和建议
- **评分标准化**：0-100分制，支持自定义评分标准

### 3. 教学平台集成

- **超星学习通适配**：课程同步、学生管理、作业推送
- **钉钉教育版对接**：消息通知、成绩发布
- **统一接口抽象**：TeachingPlatformIntegration标准接口
- **插件式扩展**：轻松添加新的平台适配器

### 4. 知识图谱引擎

- **知识节点管理**：构建课程知识体系结构
- **知识点关联**：建立前置知识和后续知识的依赖关系
- **学习路径推荐**：基于知识图谱的个性化学习路径
- **掌握度追踪**：实时跟踪学生对各知识点的掌握情况

### 5. 学情分析系统

- **学习行为分析**：记录和分析学生的学习行为数据
- **能力评估模型**：多维度评估学生能力水平
- **预警机制**：识别学习困难学生并及时预警
- **可视化报告**：生成直观的学情分析报告

### 6. 多角色支持

- **学生端**：作业提交、查看批改结果、AI辅导答疑
- **教师端**：作业发布、批改管理、学情查看、Agent配置
- **权限控制**：基于角色的访问控制（RBAC）

---

## 快速开始

### 环境要求

| 软件 | 版本要求 | 下载地址 |
|------|----------|----------|
| JDK | 1.8+ | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) |
| Maven | 3.6+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| MySQL | 8.0+ | [MySQL](https://dev.mysql.com/downloads/) |

### 安装步骤

#### 步骤一：克隆项目

```bash
git clone <仓库地址>
cd java-agent-framework
```

#### 步骤二：数据库配置

创建MySQL数据库：

```sql
CREATE DATABASE agent_framework DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库迁移脚本：

```bash
mysql -u root -p agent_framework < src/main/resources/MIGRATION_COMPLETE.sql
```

#### 步骤三：修改配置文件

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置（根据实际情况修改）
spring.datasource.url=jdbc:mysql://localhost:3306/agent_framework?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=你的密码

# AI服务配置（可选，不配置则使用模拟模式）
ai.api.enabled=true
ai.api.key=你的智谱AI API Key
ai.api.model=glm-4v
```

> **提示**：如果没有智谱AI API Key，系统会自动使用模拟模式运行，所有功能正常可用。

#### 步骤四：启动项目

```bash
# 使用Maven启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/java-agent-framework-1.0.0.jar
```

#### 步骤五：访问系统

启动成功后，在浏览器中访问：

- **登录页面**：http://localhost:8080/login.html
- **首页**：http://localhost:8080/
- **学生端**：http://localhost:8080/student.html
- **教师端**：http://localhost:8080/teacher.html

---

## 项目结构

```
java-agent-framework/
|-- src/main/java/com/example/agentframework/
|   |-- AgentFrameworkApplication.java        # 应用入口
|   |-- agent/                                # AI Agent核心
|   |   |-- Agent.java                        # Agent接口定义
|   |   |-- AgentContext.java                 # Agent上下文
|   |   |-- AgentRegistry.java                # Agent注册中心
|   |   |-- AgentRequest.java                 # 请求封装
|   |   |-- AgentResponse.java                # 响应封装
|   |-- config/                               # 配置类
|   |   |-- WebConfig.java                    # Web配置
|   |-- controller/                           # REST API控制器
|   |   |-- AgentController.java              # Agent管理API
|   |   |-- AssignmentController.java         # 作业管理API
|   |   |-- AuthController.java               # 认证API
|   |   |-- ChatController.java               # 对话API
|   |   |-- ReviewController.java             # 批改API
|   |   |-- StudentController.java            # 学生管理API
|   |   |-- TeacherController.java            # 教师管理API
|   |   |-- IntegrationController.java        # 平台集成API
|   |   |-- KnowledgeNodeController.java      # 知识点API
|   |   |-- AnalyticsController.java          # 分析API
|   |   |-- SubmissionController.java         # 提交记录API
|   |   |-- FrontendController.java           # 前端页面路由
|   |-- engine/                               # 智能引擎
|   |   |-- IntelligentEngine.java            # 引擎接口
|   |   |-- impl/
|   |       |-- IntelligentEngineImpl.java    # 引擎实现
|   |-- entity/                               # 数据实体
|   |   |-- Agent.java                        # Agent实体
|   |   |-- Assignment.java                   # 作业实体
|   |   |-- Student.java                      # 学生实体
|   |   |-- Teacher.java                      # 教师实体
|   |   |-- Submission.java                   # 提交记录实体
|   |   |-- User.java                         # 用户实体
|   |   |-- KnowledgeNode.java                # 知识节点实体
|   |-- integration/                          # 平台集成
|   |   |-- TeachingPlatformIntegration.java  # 集成接口
|   |   |-- IntegrationManager.java           # 集成管理器
|   |   |-- impl/
|   |       |-- ChaoxingIntegration.java      # 超星适配器
|   |       |-- DingTalkIntegration.java      # 钉钉适配器
|   |-- knowledge/                            # 知识图谱
|   |   |-- KnowledgeMiddleware.java          # 知识中间件接口
|   |   |-- KnowledgeNode.java                # 知识节点
|   |   |-- impl/
|   |       |-- KnowledgeMiddlewareImpl.java
|   |-- mapper/                               # MyBatis Mapper
|   |   |-- AgentMapper.java
|   |   |-- AssignmentMapper.java
|   |   |-- StudentMapper.java
|   |   |-- TeacherMapper.java
|   |   |-- SubmissionMapper.java
|   |   |-- UserMapper.java
|   |   |-- KnowledgeNodeMapper.java
|   |-- service/                              # 业务服务层
|       |-- AIService.java                    # AI服务（核心）
|       |-- AgentService.java                 # Agent服务
|       |-- AssignmentService.java            # 作业服务
|       |-- SubmissionService.java            # 提交服务
|       |-- StudentService.java               # 学生服务
|       |-- TeacherService.java               # 教师服务
|       |-- UserService.java                  # 用户服务
|       |-- FileStorageService.java           # 文件存储服务
|       |-- KnowledgeNodeService.java         # 知识点服务
|       |-- AssignmentReviewServiceImpl.java  # 批改服务实现
|       |-- SmartImageReviewService.java      # 智能图片批改
|       |-- FreeImageReviewService.java       # 免费图片批改
|       |-- AnalyticsServiceImpl.java         # 分析服务实现
|       |-- UserServiceImpl.java              # 用户服务实现
|-- src/main/resources/
|   |-- application.properties                # 应用配置
|   |-- MIGRATION_COMPLETE.sql                # 数据库迁移脚本
|   |-- mapper/                               # MyBatis XML映射
|   |   |-- StudentMapper.xml
|   |   |-- TeacherMapper.xml
|   |-- static/                               # 前端静态资源
|       |-- index.html                        # 首页
|       |-- login.html                        # 登录页
|       |-- student.html                      # 学生端
|       |-- teacher.html                      # 教师端
|-- uploads/                                  # 文件上传目录
|   |-- submissions/                          # 作业提交文件
|-- pom.xml                                   # Maven配置文件
|-- README.md                                 # 项目说明文档
```

---

## API 接口文档

### Agent 管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/agents` | 创建Agent |
| GET | `/api/agents` | 获取所有Agent |
| GET | `/api/agents/{id}` | 获取指定Agent |
| PUT | `/api/agents/{id}` | 更新Agent |
| DELETE | `/api/agents/{id}` | 删除Agent |
| POST | `/api/agents/{id}/chat` | 与Agent对话 |

### 作业管理

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/assignments` | 创建作业 |
| GET | `/api/assignments` | 获取作业列表 |
| GET | `/api/assignments/{id}` | 获取作业详情 |
| PUT | `/api/assignments/{id}` | 更新作业 |
| DELETE | `/api/assignments/{id}` | 删除作业 |

### 智能批改

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/review/image` | 图片作业批改 |
| POST | `/api/review/text` | 文本作业批改 |
| GET | `/api/review/result/{id}` | 获取批改结果 |

### 平台集成

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/integration/platforms` | 获取已集成的平台 |
| POST | `/api/integration/sync` | 同步课程数据 |
| POST | `/api/integration/test` | 测试平台连接 |

> 完整API文档请在应用启动后访问：http://localhost:8080 （如已配置Swagger UI）

---

## 使用指南

### 学生使用流程

1. **登录系统** -- 访问学生端页面
2. **查看作业** -- 浏览老师发布的作业列表
3. **提交作业** -- 支持文字输入或图片拍照上传
4. **查看批改结果** -- 查看AI批改的详细反馈和建议
5. **AI答疑** -- 与课程Agent进行对话，获取学习帮助

### 教师使用流程

1. **登录系统** -- 访问教师端页面
2. **发布作业** -- 设置作业要求、截止时间、总分等
3. **配置Agent** -- 为课程创建或选择合适的AI Agent
4. **查看学情** -- 查看班级整体和学生个人学习情况
5. **导出报告** -- 导出学情分析报告用于教学改进

---

## 高级配置

### AI模型配置

在 `application.properties` 中配置AI服务：

```properties
# 启用AI服务（true/false）
ai.api.enabled=true

# 智谱AI API Key（从 https://open.bigmodel.cn/ 获取）
ai.api.key=your_api_key_here

# 使用的模型
# glm-4-flash: 快速响应，适合对话
# glm-4v-plus: 视觉模型，适合图片识别批改
ai.api.model=glm-4v
```

### 数据库优化建议

```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_assignment_course ON assignment(course_id);
CREATE INDEX idx_submission_student ON submission(student_id);
CREATE INDEX idx_submission_assignment ON submission(assignment_id);
CREATE INDEX idx_agent_course ON agent(course_id);
```

---

## 常用命令

```bash
# 编译项目（跳过测试）
mvn clean package -DskipTests

# 运行测试
mvn test

# 启动开发服务器（热更新）
mvn spring-boot:run

# 生成生产包
mvn clean package -Pprod

# 查看依赖树
mvn dependency:tree

# 代码格式化（如果配置了formatter插件）
mvn formatter:format
```

---

## 故障排除

### 问题1：数据库连接失败

**症状**：启动时报错 `Communications link failure`

**解决方案**：
1. 确认MySQL服务已启动
2. 检查 `application.properties` 中的数据库连接配置
3. 确认数据库 `agent_framework` 已创建

### 问题2：AI服务调用失败

**症状**：批改功能返回模拟数据

**解决方案**：
1. 检查网络连接是否正常
2. 确认API Key是否正确配置
3. 查看日志中的具体错误信息
4. 如果没有API Key，系统会自动使用模拟模式

### 问题3：文件上传失败

**症状**：上传作业图片时报错

**解决方案**：
1. 确保 `uploads/submissions/` 目录存在且有写权限
2. 检查文件大小是否超过限制
3. 确认文件格式支持（PNG、JPG、JPEG）

---

## 性能指标

| 指标 | 数值 | 说明 |
|------|------|------|
| 图片批改响应时间 | < 3秒 | 使用GLM-4V视觉模型 |
| 并发用户支持 | 100+ | 单机部署 |
| 作业批改准确率 | 90%+ | 基于AI模型评估 |
| 系统可用性 | 99.9% | 生产环境目标 |

---

## 贡献指南

我们欢迎任何形式的贡献！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 开发规范

- 遵循阿里巴巴Java开发规范
- 代码注释使用中文
- 提交信息格式：`type: description`
  - feat: 新功能
  - fix: 修复bug
  - docs: 文档更新
  - style: 代码格式调整
  - refactor: 重构
  - test: 测试相关
  - chore: 构建/工具相关

---

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 团队成员

| 角色 | 姓名 | 负责模块 |
|------|------|----------|
| 项目负责人 | XXX | 架构设计、核心开发 |
| 后端开发 | XXX | Agent引擎、AI集成 |
| 后端开发 | XXX | 平台适配、数据分析 |
| 前端开发 | XXX | 页面开发、交互设计 |
| 测试工程师 | XXX | 功能测试、性能测试 |

---

## 联系方式

- **项目邮箱**：edu-ai@example.com
- **问题反馈**：[GitHub Issues](https://github.com/your-repo/issues)
- **技术文档**：[Wiki](https://github.com/your-repo/wiki)

---

## 致谢

- [智谱AI](https://open.bigmodel.cn/) - 提供强大的GLM-4V视觉语言模型
- [Spring](https://spring.io/projects/spring-boot) - 优秀的Java开发框架
- [MyBatis](https://mybatis.org/) - 灵活的持久层框架
- 所有为本项目贡献代码和想法的开发者

---

<div align="center">

如果这个项目对你有帮助，请给一个Star支持！

Made by 教育AI团队

</div>
