# 可嵌入式跨课程AI Agent通用架构平台

**面向高等教育的智能化教学辅助系统**

[Java 8+](https://www.oracle.com/java/technologies/downloads/) | [Spring Boot 2.7.15](https://spring.io/projects/spring-boot) | [MySQL 8.0+](https://dev.mysql.com/) | [Redis 7](https://redis.io/) | [Milvus 2.3.4](https://milvus.io/) | [智谱AI GLM-4V](https://open.bigmodel.cn/) | [Docker](https://www.docker.com/) | [MIT License](LICENSE)

***

## 项目简介

本项目是一个可嵌入式的跨课程AI Agent通用架构平台，专为高等教育场景设计。平台采用模块化Agent架构，集成RAG向量检索、BKT知识追踪、工作流引擎等核心能力，能够无缝对接超星学习通、钉钉等主流教学平台，提供智能作业批改、学情分析、个性化辅导等完整功能。

### 核心价值

- **可嵌入式设计**：Agent架构支持灵活嵌入不同课程和教学场景
- **跨课程通用**：一套系统适配多学科、多课程的教学需求
- **平台无缝对接**：原生支持超星、钉钉等主流教学平台
- **AI智能驱动**：基于智谱GLM-4V视觉大模型，支持图片作业智能批改
- **RAG语义检索**：Milvus向量数据库实现知识检索与推荐
- **BKT知识追踪**：实时追踪学生对各知识点的掌握情况
- **数据驱动教学**：提供多维度学情分析，助力精准教学

***

## 系统架构

```
+------------------------------------------------------------------------------------+
|                               前端展示层 (HTML+Vue+ElementUI)                       |
|   +---------------+    +---------------+    +---------------+                    |
|   |  学生端页面    |    |  教师端页面    |    |  管理端页面    |                    |
|   +-------+-------+    +-------+-------+    +-------+-------+                    |
+-----------+---------------------+---------------------+--------------------------+
|           |                     |                     |
+-----------v---------------------v---------------------v--------------------------+
|                          Nginx 反向代理 & 静态资源服务                                 |
+------------------------------------------+------------------------------------------+
                                           |
+------------------------------------------v------------------------------------------+
|                          Spring Boot 后端应用层                                      |
|   +-------------------+  +-------------------+  +-------------------+            |
|   |   前端Controller  |  |   业务Controller   |  |   API Controller   |            |
|   +-------------------+  +-------------------+  +-------------------+            |
+------------------------------+------------------------------+------------------+
|                              |                              |
+------------------------------v------------------------------v------------------+
|                          业务逻辑层 (Service)                                           |
|   +-------------------+  +-------------------+  +-------------------+            |
|   |  Agent服务        |  |  智能引擎        |  |  集成管理器      |            |
|   |  作业服务        |  |  RAG服务        |  |  文件存储        |            |
|   |  批改服务        |  |  BKT服务        |  |  AI服务          |            |
|   |  分析服务        |  |  知识图谱        |  |  工作流引擎      |            |
|   +-------------------+  +-------------------+  +-------------------+            |
+-------------------------------+-------------------------------+-----------------+
|                               |                               |
+-------------------------------v-------------------------------v-----------------+
|                      数据持久层 & 缓存层 & 向量层                               |
|                                                                                    |
|   +-------------------+  +-------------------+  +-------------------+            |
|   |  MySQL 8.0+      |  |  Redis 7         |  |  Milvus 2.3.4     |            |
|   |  (关系数据库)    |  |  (缓存)          |  |  (向量数据库)    |            |
|   +-------------------+  +-------------------+  +-------------------+            |
|                                                                                    |
|   MyBatis ORM  |  Spring Data Redis  |  Embedding (智谱AI)                      |
+-----------------------------------------------------------------------------------+

                              外部服务集成
    +-----------------+  +-----------------+  +-----------------+
    |  超星学习通     |  |   钉钉教育版    |  |  智谱AI GLM    |
    +-----------------+  +-----------------+  +-----------------+
```

***

## 技术栈

| 类别      | 技术             | 版本         | 说明              |
| ------- | -------------- | ---------- | --------------- |
| 后端框架    | Spring Boot    | 2.7.15     | 企业级Java开发框架     |
| ORM框架   | MyBatis        | 2.3.1      | 轻量级持久层框架        |
| 关系数据库   | MySQL          | 8.0+       | 关系型数据库          |
| 缓存数据库   | Redis          | 7.x        | 内存缓存数据库         |
| 向量数据库   | Milvus         | 2.3.4      | 向量搜索数据库         |
| 元数据存储   | etcd           | 3.5.5      | 分布式键值存储         |
| 对象存储    | MinIO          | 2023.03.20 | 对象存储服务          |
| 容器化     | Docker Compose | 3.8        | 容器编排            |
| 反向代理    | Nginx          | latest     | 负载均衡、静态资源       |
| AI引擎    | 智谱AI GLM-4V    | -          | 视觉语言大模型         |
| HTTP客户端 | OkHttp         | 4.12.0     | 高性能HTTP请求库      |
| JSON处理  | Gson           | 2.10.1     | Google JSON库    |
| 工具库     | Lombok         | 1.18.30    | 简化Java代码        |
| 前端框架    | Vue            | 2.x        | 渐进式JavaScript框架 |
| UI组件库   | Element UI     | -          | Vue组件库          |
| 图表库     | ECharts        | -          | 数据可视化库          |

***

## 核心功能模块

### 1. AI Agent 系统

- **可扩展的Agent架构**：支持自定义Agent类型和角色
- **上下文感知**：AgentContext提供完整的运行时环境
- **生命周期管理**：初始化、处理请求、学习、重置完整流程
- **多Agent协作**：支持同一课程部署多个专业Agent
- **内置专业Agent**：
  - 📚 **Tutor Agent**：智能辅导答疑
  - 📝 **Grader Agent**：智能作业批改
  - 📊 **Analyst Agent**：学情数据分析

### 2. 工作流引擎

- **可视化工作流编排**：灵活组合多个Agent执行复杂任务
- **三大学科工作流**：
  - **智能辅导工作流**：Analyst → Tutor → Analyst
  - **智能批改工作流**：Grader → Grader → Analyst
  - **学情分析工作流**：Analyst → Analyst → Analyst
- **持久化执行**：Redis缓存执行历史，支持断点续传
- **健康检查**：自动检测并降级故障服务

### 3. RAG检索增强生成

- **向量数据库**：Milvus存储知识节点向量
- **语义检索**：基于余弦相似度的智能推荐
- **Embedding服务**：支持智谱AI向量模型
- **本地降级**：API不可用时自动使用本地模拟向量
- **知识图谱**：知识点关联与推荐

### 4. BKT知识追踪

- **实时掌握度追踪**：跟踪学生对各知识点的掌握情况
- **个性化学习路径**：基于掌握度推荐学习内容
- **贝叶斯知识追踪模型**：科学的学习预测算法
- **练习推荐**：基于错题自动生成巩固练习题

### 5. 智能作业批改

- **图片识别批改**：支持手写作业、拍照上传的图片识别与批改
- **多维度评价**：从正确性、完整性、规范性等多维度评分
- **智能反馈生成**：自动生成详细的批改意见和建议
- **评分标准化**：0-100分制，支持自定义评分标准
- **JSON反馈**：结构化的评分与评语

### 6. 教学平台集成

- **超星学习通适配**：课程同步、学生管理、作业推送
- **钉钉教育版对接**：消息通知、成绩发布
- **统一接口抽象**：TeachingPlatformIntegration标准接口
- **插件式扩展**：轻松添加新的平台适配器

### 7. 学情分析系统

- **学习行为分析**：记录和分析学生的学习行为数据
- **能力评估模型**：多维度评估学生能力水平
- **预警机制**：识别学习困难学生并及时预警
- **可视化报告**：生成直观的学情分析报告（ECharts图表）

### 8. 文件管理系统

- **附件上传下载**：教师发布作业附件，学生查看下载
- **图片预览**：支持图片预览、缩放、旋转
- **PDF在线查看**：浏览器原生PDF渲染
- **文件安全**：防止路径遍历攻击

### 9. 多角色支持

- **学生端**：作业提交、查看批改结果、AI辅导答疑、附件查看
- **教师端**：作业发布、批改管理、学情查看、Agent配置、附件上传
- **权限控制**：基于角色的访问控制（RBAC）

***

## 快速开始

### 方式一：Docker Compose 一键部署（推荐）

#### 环境要求

| 软件             | 版本要求   | 说明     |
| -------------- | ------ | ------ |
| Docker         | 20.10+ | 容器引擎   |
| Docker Compose | 2.0+   | 容器编排工具 |

#### 部署步骤

1. **克隆项目**

```bash
git clone <仓库地址>
cd java-agent-framework
```

1. **配置环境变量**

```bash
# 复制示例配置文件
cp .env.example .env

# 编辑 .env 文件（可选，默认配置已可运行）
```

1. **启动所有服务**

```bash
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f mysql redis milvus
```

1. **初始化数据库**

```bash
# 等待MySQL启动完成后执行
docker-compose exec mysql mysql -uroot -p123456 agent_framework < src/main/resources/MIGRATION_COMPLETE.sql
```

1. **访问系统**

```
# 学生端
http://localhost/static/student.html

# 教师端
http://localhost/static/teacher.html
```

***

### 方式二：本地开发环境部署

#### 环境要求

| 软件     | 版本要求  | 下载地址                                                                 |
| ------ | ----- | -------------------------------------------------------------------- |
| JDK    | 1.8+  | [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)    |
| Maven  | 3.6+  | [Apache Maven](https://maven.apache.org/download.cgi)                |
| MySQL  | 8.0+  | [MySQL](https://dev.mysql.com/downloads/)                            |
| Redis  | 7.x   | [Redis](https://redis.io/download)                                   |
| Milvus | 2.3.4 | [Milvus Docker](https://milvus.io/docs/install_standalone-docker.md) |

#### 部署步骤

1. **克隆项目**

```bash
git clone <仓库地址>
cd java-agent-framework
```

1. **配置数据库**

创建MySQL数据库：

```sql
CREATE DATABASE agent_framework DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库迁移脚本：

```bash
mysql -u root -p agent_framework < src/main/resources/MIGRATION_COMPLETE.sql
```

1. **修改配置文件**

编辑 `src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/agent_framework?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=你的密码

# Redis配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.enabled=true

# Milvus配置
milvus.host=localhost
milvus.port=19530
milvus.collection.name=knowledge_nodes

# AI服务配置（可选）
ai.api.enabled=true
ai.api.key=你的智谱AI API Key
ai.api.model=glm-4v-plus

# 文件上传限制
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
```

1. **启动依赖服务（可选，如果本地已有MySQL/Redis）**

```bash
# 使用Docker仅启动MySQL、Redis、Milvus
docker-compose up -d mysql redis milvus-etcd milvus-minio milvus
```

1. **启动项目**

```bash
# 使用Maven启动
mvn spring-boot:run

# 或者打包后运行
mvn clean package -DskipTests
java -jar target/java-agent-framework-1.0.0.jar
```

1. **访问系统**

```
# 首页
http://localhost:8080/

# 登录页面
http://localhost:8080/static/login.html

# 学生端
http://localhost:8080/static/student.html

# 教师端
http://localhost:8080/static/teacher.html
```

***

## Docker Compose 服务说明

### 服务列表

| 服务名称         | 镜像                                       | 端口映射                   | 说明          | 数据持久化        |
| ------------ | ---------------------------------------- | ---------------------- | ----------- | ------------ |
| mysql        | mysql:8.0                                | 3306:3306              | 关系数据库       | mysql\_data  |
| redis        | redis:7-alpine                           | 6379:6379              | 缓存服务        | redis\_data  |
| milvus-etcd  | quay.io/coreos/etcd:v3.5.5               | -                      | Milvus元数据存储 | etcd\_data   |
| milvus-minio | minio/minio:RELEASE.2023-03-20T20-16-18Z | 9000:9000, 9001:9001   | Milvus对象存储  | minio\_data  |
| milvus       | milvusdb/milvus:v2.3.4                   | 19530:19530, 9091:9091 | 向量数据库       | milvus\_data |
| app          | (Dockerfile构建)                           | 8080:8080              | Java应用      | upload\_data |
| nginx        | nginx:alpine                             | 80:80                  | 反向代理        | -            |

### 常用Docker命令

```bash
# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f mysql
docker-compose logs -f app

# 重启服务
docker-compose restart app

# 停止所有服务
docker-compose down

# 停止并删除数据卷（谨慎使用！）
docker-compose down -v

# 重新构建并启动
docker-compose up -d --build app

# 进入MySQL容器
docker-compose exec mysql mysql -uroot -p123456 agent_framework
```

***

## 项目结构

```
java-agent-framework/
|-- src/main/java/com/example/agentframework/
|   |-- AgentFrameworkApplication.java                # 应用入口
|   |-- agent/                                        # AI Agent核心
|   |   |-- Agent.java                                # Agent接口定义
|   |   |-- AgentContext.java                         # Agent上下文
|   |   |-- AgentRegistry.java                        # Agent注册中心
|   |   |-- AgentRequest.java                         # 请求封装
|   |   |-- AgentResponse.java                        # 响应封装
|   |-- bkt/                                          # BKT知识追踪
|   |   |-- BKTModel.java                             # BKT模型
|   |   |-- BKTService.java                           # BKT服务
|   |-- config/                                       # 配置类
|   |   |-- WebConfig.java                            # Web配置
|   |   |-- FileConfig.java                           # 文件配置
|   |-- controller/                                   # REST API控制器
|   |   |-- AgentController.java                      # Agent管理API
|   |   |-- AssignmentController.java                 # 作业管理API
|   |   |-- AuthController.java                       # 认证API
|   |   |-- ChatController.java                       # 对话API
|   |   |-- ReviewController.java                     # 批改API
|   |   |-- FileController.java                       # 文件API
|   |   |-- StudentController.java                    # 学生管理API
|   |   |-- TeacherController.java                    # 教师管理API
|   |-- engine/                                       # 智能引擎
|   |   |-- IntelligentEngine.java                    # 引擎接口
|   |   |-- impl/
|   |       |-- IntelligentEngineImpl.java            # 引擎实现
|   |-- entity/                                       # 数据实体
|   |   |-- Agent.java                                # Agent实体
|   |   |-- Assignment.java                           # 作业实体
|   |   |-- Student.java                              # 学生实体
|   |   |-- Submission.java                           # 提交记录实体
|   |   |-- KnowledgeNode.java                        # 知识节点实体
|   |-- mapper/                                       # MyBatis Mapper
|   |   |-- AgentMapper.java
|   |   |-- AssignmentMapper.java
|   |   |-- StudentMapper.java
|   |   |-- SubmissionMapper.java
|   |   |-- KnowledgeNodeMapper.java
|   |-- rag/                                          # RAG向量检索
|   |   |-- EmbeddingService.java                     # 向量生成服务
|   |   |-- RAGService.java                           # RAG服务
|   |   |-- VectorStoreService.java                   # 向量存储服务
|   |-- service/                                      # 业务服务层
|       |-- AIService.java                            # AI服务（核心）
|       |-- AssignmentService.java                    # 作业服务
|       |-- FileStorageService.java                   # 文件存储服务
|       |-- AssignmentReviewServiceImpl.java          # 批改服务实现
|       |-- AnalyticsServiceImpl.java                 # 分析服务实现
|-- src/main/resources/
|   |-- application.properties                        # 应用配置
|   |-- MIGRATION_COMPLETE.sql                        # 数据库迁移脚本
|   |-- BKT_WORKFLOW_MIGRATION.sql                    # 工作流迁移脚本
|   |-- mapper/                                       # MyBatis XML映射
|   |-- static/                                       # 前端静态资源
|       |-- index.html                                # 首页
|       |-- login.html                                # 登录页
|       |-- student.html                              # 学生端
|       |-- teacher.html                              # 教师端
|       |-- css/                                      # 样式文件
|       |-- js/                                       # 脚本文件
|       |-- lib/                                      # 第三方库
|-- uploads/                                          # 文件上传目录
|   |-- submissions/                                  # 作业提交文件
|-- docker-compose.yml                                # Docker编排配置
|-- nginx.conf                                        # Nginx配置
|-- Dockerfile                                        # Docker镜像构建
|-- pom.xml                                           # Maven配置文件
|-- README.md                                         # 项目说明文档
```

***

## API 接口文档

### Agent 管理

| 方法     | 路径                      | 说明        |
| ------ | ----------------------- | --------- |
| POST   | `/api/agents`           | 创建Agent   |
| GET    | `/api/agents`           | 获取所有Agent |
| GET    | `/api/agents/{id}`      | 获取指定Agent |
| PUT    | `/api/agents/{id}`      | 更新Agent   |
| DELETE | `/api/agents/{id}`      | 删除Agent   |
| POST   | `/api/agents/{id}/chat` | 与Agent对话  |

### 作业管理

| 方法     | 路径                                        | 说明     |
| ------ | ----------------------------------------- | ------ |
| POST   | `/api/assignments`                        | 创建作业   |
| GET    | `/api/assignments`                        | 获取作业列表 |
| GET    | `/api/assignments/{id}`                   | 获取作业详情 |
| PUT    | `/api/assignments/{id}`                   | 更新作业   |
| DELETE | `/api/assignments/{id}`                   | 删除作业   |
| POST   | `/api/assignments/{id}/upload-attachment` | 上传作业附件 |

### 文件管理

| 方法  | 路径                               | 说明     |
| --- | -------------------------------- | ------ |
| GET | `/api/files/{filename}`          | 在线预览文件 |
| GET | `/api/files/download/{filename}` | 下载文件   |
| GET | `/api/files/info/{filename}`     | 获取文件信息 |

### 智能批改

| 方法   | 路径                        | 说明     |
| ---- | ------------------------- | ------ |
| POST | `/api/review/image`       | 图片作业批改 |
| POST | `/api/review/text`        | 文本作业批改 |
| GET  | `/api/review/result/{id}` | 获取批改结果 |

### 工作流管理

| 方法   | 路径                       | 说明        |
| ---- | ------------------------ | --------- |
| GET  | `/api/workflows`         | 获取所有工作流定义 |
| POST | `/api/workflows/execute` | 执行工作流     |
| GET  | `/api/workflows/history` | 获取工作流执行历史 |

### RAG检索

| 方法   | 路径               | 说明     |
| ---- | ---------------- | ------ |
| POST | `/api/rag/query` | 语义检索查询 |
| POST | `/api/rag/add`   | 添加知识节点 |

### BKT知识追踪

| 方法   | 路径                               | 说明         |
| ---- | -------------------------------- | ---------- |
| GET  | `/api/bkt/{studentId}/mastery`   | 获取学生知识点掌握度 |
| POST | `/api/bkt/{studentId}/update`    | 更新学习记录     |
| POST | `/api/bkt/{studentId}/recommend` | 推荐学习内容     |

***

## 使用指南

### 学生使用流程

1. **登录系统** -- 访问学生端页面
2. **查看作业** -- 浏览老师发布的作业列表
3. **查看附件** -- 点击查看或下载作业附件
4. **提交作业** -- 支持文字输入或图片拍照上传
5. **查看批改结果** -- 查看AI批改的详细反馈和建议
6. **错题分析** -- 查看智能错题分析与巩固建议
7. **练习巩固** -- 点击生成针对性练习题
8. **AI答疑** -- 与课程Agent进行对话，获取学习帮助

### 教师使用流程

1. **登录系统** -- 访问教师端页面
2. **发布作业** -- 设置作业要求、截止时间、总分、作业类型、评分标准
3. **上传附件** -- 可选上传PDF、图片等作业附件
4. **配置Agent** -- 为课程创建或选择合适的AI Agent
5. **查看学情** -- 查看班级整体和学生个人学习情况
6. **导出报告** -- 导出学情分析报告用于教学改进

***

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
ai.api.model=glm-4v-plus

# Embedding模型
ai.embedding.model=embedding-3
```

### Milvus向量数据库配置

```properties
# Milvus连接配置
milvus.host=localhost
milvus.port=19530
milvus.collection.name=knowledge_nodes
milvus.index.type=IVF_FLAT
milvus.metric.type=COSINE
milvus.nlist=1024
milvus.nprobe=16
milvus.topk=5
milvus.embedding.dimension=1024
```

### Redis缓存配置

```properties
# Redis连接配置
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=
spring.redis.database=0
spring.redis.enabled=true
```

### 数据库优化建议

```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_assignment_course ON assignment(course_id);
CREATE INDEX idx_submission_student ON submission(student_id);
CREATE INDEX idx_submission_assignment ON submission(assignment_id);
CREATE INDEX idx_agent_course ON agent(course_id);
CREATE INDEX idx_knowledge_course ON knowledge_node(course_id);
CREATE INDEX idx_student_mastery ON student_mastery(student_id, knowledge_id);
```

***

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

# Docker相关
docker-compose up -d                # 启动所有服务
docker-compose down                 # 停止所有服务
docker-compose restart app          # 重启应用服务
docker-compose logs -f              # 查看实时日志
docker-compose exec mysql bash      # 进入MySQL容器
```

***

## 故障排除

### 问题1：数据库连接失败

**症状**：启动时报错 `Communications link failure`

**解决方案**：

1. 确认MySQL服务已启动
2. 检查 `application.properties` 中的数据库连接配置
3. 确认数据库 `agent_framework` 已创建
4. 如果使用Docker，检查端口映射是否正确（3306:3306）

### 问题2：文件上传无法保存

**症状**：附件已保存到uploads/目录，但数据库中file\_path和file\_name为null

**解决方案**：

1. 确认 `AssignmentMapper.java` 中的INSERT和UPDATE已包含file\_path和file\_name字段
2. 删除target/目录后重新编译：`mvn clean compile`
3. 重启应用

### 问题3：AI服务调用失败

**症状**：批改功能返回模拟数据

**解决方案**：

1. 检查网络连接是否正常
2. 确认API Key是否正确配置
3. 查看日志中的具体错误信息
4. 如果没有API Key，系统会自动使用模拟模式，所有功能仍然可用

### 问题4：Redis/Milvus连接失败

**症状**：启动时警告服务不可用

**解决方案**：

1. 确认Redis/Milvus容器已启动
2. 检查端口是否被占用
3. 系统已实现降级机制，即使失败也会继续运行（使用内存缓存/本地向量模拟）

***

## 性能指标

| 指标         | 数值      | 说明           |
| ---------- | ------- | ------------ |
| 图片批改响应时间   | < 3秒    | 使用GLM-4V视觉模型 |
| RAG向量检索    | < 100ms | Milvus向量搜索   |
| 并发用户支持     | 100+    | 单机部署         |
| 作业批改准确率    | 90%+    | 基于AI模型评估     |
| 系统可用性      | 99.9%   | 生产环境目标       |
| MySQL查询响应  | < 50ms  | 索引优化         |
| Redis缓存命中率 | > 85%   | 工作流数据缓存      |

***

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

***

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

***

## 团队成员

| 角色    | 姓名  | 负责模块          |
| ----- | --- | ------------- |
| 项目负责人 | XXX | 架构设计、核心开发     |
| 后端开发  | XXX | Agent引擎、RAG集成 |
| 后端开发  | XXX | BKT模型、工作流引擎   |
| 后端开发  | XXX | 平台适配、数据分析     |
| 前端开发  | XXX | 页面开发、交互设计     |
| 测试工程师 | XXX | 功能测试、性能测试     |

***

## 联系方式

- **项目邮箱**：<edu-ai@example.com>
- **问题反馈**：[GitHub Issues](https://github.com/your-repo/issues)
- **技术文档**：[Wiki](https://github.com/your-repo/wiki)

***

## 致谢

- [智谱AI](https://open.bigmodel.cn/) - 提供强大的GLM-4V视觉语言模型
- [Spring](https://spring.io/projects/spring-boot) - 优秀的Java开发框架
- [MyBatis](https://mybatis.org/) - 灵活的持久层框架
- [Milvus](https://milvus.io/) - 高性能向量数据库
- [Redis](https://redis.io/) - 优秀的内存数据库
- 所有为本项目贡献代码和想法的开发者

***

<div align="center">

如果这个项目对你有帮助，请给一个Star支持！

Made by 教育AI团队

</div>
