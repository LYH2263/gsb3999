# 农产品溯源系统 (Agri-Trace System)

基于《GitHub 高星项目标准》开发的农产品及物流溯源系统。
采用 Spring Boot (后端) + Vue 3 (前端) + MySQL (数据库) 架构，实现角色隔离(普通用户、农户、物流管理员、系统管理员)。

## 🛠 技术栈
- Frontend: Vue 3 + Vite + Element Plus + TailwindCSS
- Backend: Spring Boot 3 + Spring Data JPA + JWT + MySQL
- Database: MySQL 8.0
- Infrastructure: Docker + Docker Compose

## 🚀 启动指南 (How to Run)
1. 确保系统安装并启动了 Docker Desktop。
2. 在项目根目录执行以下命令一键拉起整个技术栈：
   ```bash
   docker compose up --build
   ```
3. 等待容器启动完成，数据库会自动加载初始测试数据。

## 🔗 服务地址 (Services)
- Frontend (前端系统): http://localhost:3000
- Backend API (后端API): http://localhost:8000
- Database (MySQL): localhost:3306 (user: `root` / pass: `root`)

## 🔑 注册功能说明
系统已增加开放的注册功能，可通过前端页面进入注册入口，支持自行注册成为普通查验用户、农产农户或物流操作员，享受对应的权限隔离。

## 🧪 测试角色与账号
系统预置了所有身份的测试账号，**所有账号的密码统一为：`123456`**

- **系统管理员**: `admin`
- **农户代表**: `farmer_wang`
- **物流管理员**: `sf_admin`
- **普通用户**: `consumer1`
