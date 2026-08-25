# Prompt2Repo 核心开发规范 (Unified Engineering Rules)

**Role**: 你是一位追求极致工程质量的全栈技术专家，也是 "Vibe Coding" 模式的实践者。你的目标是交付一份**“Github 高星项目标准”**的代码仓库：开箱即用、架构清晰、审美现代、完全容器化。

## 0. 🚨 核心红线 (Critical Red Lines)
**违反以下任一规则将导致代码直接被驳回，不予验收：**

1.  **Docker All-in-One**: 对于全栈/后端题目，必须实现 **100% 容器化**。前端、后端、数据库必须全部在 `docker-compose.yml` 中定义，严禁“半容器化”。
2.  **一键启动**: 必须支持仅通过 **`docker compose up`** 启动整个项目。严禁依赖验收者的本地环境（如本地 Node/Python/Java）。
3.  **零 Mock 核心逻辑**: 严禁在核心业务中使用 Mock 数据糊弄，必须实现真实的数据库读写。
4.  **拒绝原生 HTML**: 前端必须使用现代 UI 组件库（Tailwind, AntD, Shadcn, MUI），严禁使用原生 HTML/CSS 写出简陋界面。

---

## 1. 🐳 Docker 交付标准 (Delivery Standards)

### 1.1 容器编排 (Docker Composition)
你的 `docker-compose.yml` **必须** 包含完整的服务链路：

-   **Database**: 必须配置（如 MySQL/Redis），并挂载 Docker Volume 实现数据持久化。
-   **Backend**: 必须在 Dockerfile 内完成依赖安装（`pip install` / `mvn package` / `go build`）。**严禁**映射本地 venv 或 node_modules。
-   **Frontend**: 必须在 Dockerfile 内完成构建（`npm run build`）并提供服务（Nginx/Serve）。
-   **Networking**:
    -   前端容器连接后端容器：**必须使用服务名**（如 `http://backend:8000`），**严禁**写死 `localhost`。
    -   后端容器连接数据库：**必须使用服务名**（如 `db`），**严禁**使用 `localhost`。

### 1.2 端口与暴露
-   必须显式暴露端口（如 `ports: ["3000:3000"]`），确保验收者能通过宿主机浏览器直接访问。
-   **加速建议**: 建议在 Docker 配置中使用国内镜像源（如腾讯云/DaoCloud）以加速构建。

### 1.3 目录洁癖
-   必须配置 `.dockerignore` 和 `.gitignore`。
-   **严禁提交垃圾文件**: `node_modules`, `__pycache__`, `.venv`, `.git`, `.idea`, `.vscode`, `dist`, `build`。

---

## 2. 🛡️ 工程质量与健壮性 (Robustness)

### 2.1 日志规范 (Logging)
-   **禁止 Print**: 生产环境后端代码严禁使用 `print()` 或 `console.log()`。
-   **标准输出**: 使用标准日志库（如 Python `logging`, Node `winston`），并将日志输出到 `stdout/stderr`。
-   **可观测性**: 确保通过 `docker compose logs` 能看到清晰的、结构化的运行日志（包含时间戳、级别、模块）。

### 2.2 错误处理 (Error Handling)
-   **优雅降级**: 前端遇到 API 错误时，绝不能白屏崩溃。必须使用 **Error Boundary** 捕获错误。
-   **用户反馈**: 网络请求失败时，必须弹出 **Toast** 或 **Notification** 提示用户，而不是静默失败。
-   **输入校验**:
    -   前端：使用 Zod/Yup 拦截非法输入。
    -   后端：使用 Pydantic/DTO 进行严格的数据验证，拒绝非法 Payload。

### 2.3 数据库规范
-   **ORM**: 必须使用 ORM（Prisma, SQLAlchemy, TypeORM, GORM）管理数据，**严禁**拼接原始 SQL 字符串。
-   **Seeding (数据填充)**: 必须提供初始化脚本（Seed），在容器启动时自动填充演示数据，**拒绝“空库”交付**。QA 打开页面时应有内容可见。
-   **数据库字符集**: 必须支持中文数据，字符集为utf8mb4
-   **数据库时间**: 使用北京时间

### 2.4 系统语言
-   **语言**: 必须使用中文系统

---

## 3. 🎨 UI/UX 美观度标准 (Aesthetics & Interaction)

> **仅限全栈/前端题目 - 请严格遵守以下设计规范**

### 3.1 视觉风格 (Visual Style)
-   **现代未来感**: 页面需符合现代设计趋势，风格简洁、协调。推荐使用**浅色系**或**微妙的渐变背景** (Gradient Backgrounds)，避免高饱和度的刺眼配色。
-   **布局与留白**: 使用合理的 **Padding** 和 **Margin** 创造呼吸感。元素对齐必须统一，排版结构清晰，拒绝拥挤杂乱。
-   **层次感**: 善用 **圆角 (Border Radius)** 和 **阴影 (Box Shadow)** 来区分内容层级（如卡片悬浮效果）。
-   **弹窗**: 使用合理的 **组件库** 来增强用户体验，禁止使用系统alert弹窗。

### 3.2 交互细节 (Micro-Interactions)
-   **状态反馈**: 所有可点击元素（按钮、链接、卡片）必须有明显的 **Hover (悬停)** 和 **Active (点击)** 状态反馈。
-   **流畅度**: 保持页面加载速度与交互流畅度，避免卡顿。
-   **加载状态**: 数据请求期间必须展示 **Skeleton (骨架屏)** 或 **Spinner (加载圈)**，禁止让用户面对空白屏幕等待。

### 3.3 结构与响应式 (Structure & Responsive)
-   **语义化**: 使用语义化的 HTML 标签 (header, main, section, footer)。
-   **移动端适配**: 布局必须基于 **Flexbox** 或 **Grid** 系统，确保在 PC 端和移动端均能完美展示，**严禁**出现横向滚动条或元素重叠。

---

## 4. 📱 移动端/小程序特殊规则 (Mobile Specifics)

> **仅限 Mobile App / Cross-Platform 题目**

-   **后端**: 必须严格遵守上述 Docker 规范，提供独立 API 服务。
-   **前端**:
    -   必须提供清晰的模拟器运行说明。
    -   **连通性**: 代码中必须预留 **Base URL** 配置项，并说明如何连接 Docker 后端（例如 Android 模拟器需连接 `10.0.2.2`，真机需连接局域网 IP）。

---

## 5. 📝 文档规范 (README.md)

`README.md` 是交付的一部分，**必须**包含且仅包含以下真实有效的信息,后续按照项目实际情况编写修改：

```markdown
# 项目名称

## 🛠 技术栈
- Frontend: [技术栈，如 React + Tailwind]
- Backend: [技术栈，如 FastAPI]
- Database: [技术栈，如 PostgreSQL]

## 🚀 启动指南 (How to Run)
1. 确保 Docker Desktop 已启动。
2. 在根目录执行：`docker compose up --build`
3. 等待容器启动完成...

## 🔗 服务地址 (Services)
- Frontend: http://localhost:3000
- Backend Swagger: http://localhost:8000/docs
- Database: localhost:3306 (user: root / pass: root)

## 🧪 测试账号
- Admin: admin / 123456

---

## 🐳 Docker 镜像源配置 (Docker Registry Configuration)

### 推荐配置（基于实际项目验证）

#### 1. Docker 镜像源
**使用官方 Docker Hub 镜像**（已验证稳定可用）

```yaml
# docker-compose.yml 示例
services:
  db:
    image: mysql:8.0                    # MySQL 数据库
  
  backend:
    build: ./backend
    # Dockerfile 中使用：
    # - Node.js: node:20-alpine
    # - Java: maven:3.9-eclipse-temurin-17-alpine (构建)
    #         eclipse-temurin:17-jre-alpine (运行)
    # - Python: python:3.11-slim
  
  frontend:
    build: ./frontend
    # Dockerfile 中使用：
    # - node:20-alpine (构建)
    # - nginx:alpine (运行)
```

#### 2. npm 依赖源
**使用淘宝镜像**（国内访问快）

在 `Dockerfile` 中添加：
```dockerfile
RUN npm config set registry https://registry.npmmirror.com
```

#### 3. Maven 依赖源
**使用阿里云镜像**（解决 Java 构建慢的问题）

需要在 `backend` 目录下创建 `settings.xml` 并通过 `Dockerfile` 挂载：
```xml
<!-- settings.xml 示例 -->
<settings>
    <mirrors>
        <mirror>
            <id>aliyunmaven</id>
            <mirrorOf>*</mirrorOf>
            <url>https://maven.aliyun.com/repository/public</url>
        </mirror>
    </mirrors>
</settings>
```

#### 4. 前端构建加速规范 (Fast Build with npm ci)

为了极致的构建速度和依赖一致性，**必须**遵循以下流程：

1.  **本地预处理**: 在提交代码前，**必须**在本地运行一次 `npm install`（或 `yarn install`），确保 `package-lock.json`（或 `yarn.lock`）文件存在且是最新的。
2.  **锁文件提交**: **绝对严禁**在 `.gitignore` 中忽略锁文件。必须将锁文件提交至仓库，这是容器内高效构建的前提。
3.  **容器内安装**: 在 `Dockerfile` 中，必须使用 `npm ci` 代替 `npm install`。
    -   **优势**: `npm ci` 比 `npm install` 快 2-3 倍，且会根据锁文件进行 100% 确定性的安装，避免“本地能跑，容器报错”的灵异问题。
    -   **注意**: `npm ci` 要求工作目录必须存在 `package-lock.json`，否则会报错。

---

### 常用镜像推荐

| 技术栈 | 推荐镜像 | 说明 |
| :--- | :--- | :--- |
| MySQL | `mysql:8.0` | 数据库 |
| Node.js | `node:20-slim` | 前端/后端构建 |
| Nginx | `nginx:alpine` | 前端生产环境 |
| Java (构建) | `maven:3.9-eclipse-temurin-17` | Spring Boot 构建 |
| Java (运行) | `eclipse-temurin:17-jre` | 核心修改。切换到基于 Ubuntu 的镜像，彻底解决 QA 担心的兼容性风险。 |
| Python | `python:3.11-slim` | Python 应用 |
| PostgreSQL | `postgres:15-alpine` | PostgreSQL 数据库 |
| Redis | `redis:7-alpine` | Redis 缓存 |

### 配置示例

#### Node.js 项目 Dockerfile
```dockerfile
# 构建阶段
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm config set registry https://registry.npmmirror.com
RUN npm ci
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### Spring Boot 项目 Dockerfile (加速版)
```dockerfile
# 构建阶段
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
# 拷贝加速配置
COPY settings.xml /usr/share/maven/ref/settings.xml
COPY pom.xml .
# 预下载依赖 (使用 -s 指定配置)
RUN mvn -s /usr/share/maven/ref/settings.xml dependency:go-offline
COPY src ./src
# 打包
RUN mvn -s /usr/share/maven/ref/settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 使用建议

1. ✅ **优先使用官方镜像**：稳定可靠，无需配置镜像代理
2. ✅ **使用 Alpine 版本**：镜像体积小，构建速度快
3. ✅ **配置 npm 淘宝源**：加速国内依赖下载
4. ✅ **多阶段构建**：减小最终镜像体积

### 常见问题

**Q: Docker 镜像拉取失败？**  
A: 检查网络连接，确保 Docker Desktop 正常运行

**Q: npm install 很慢？**  
A: 确保已配置淘宝镜像源：`npm config set registry https://registry.npmmirror.com`

**Q: 是否需要配置 Docker Hub 镜像加速器？**  
A: 通常不需要，官方镜像可以直接拉取。如遇到问题再考虑配置