# 农产品溯源系统设计文档 (System Design)

基于 `<user_rule.md>` 中的【Github 高星项目标准】和您的需求，我规划了本系统的核心模块、权限矩阵和技术实现思路，并应用了 `@brainstorming` 的严谨分析模式来确保各角色相互隔离，且体验满足规范。

## 1. 核心模块划分

为了保持系统的简洁性与职责分离，后端与前端的接口功能将围绕以下四大核心模块展开：

- **身份认证与用户模块 (Auth & User Module)**
  - 角色鉴权，登录/注册逻辑。
  - 用户必须在注册或由系统创建时绑定唯一个人/企业身份角色。
- **农产品管理模块 (Product Module)**
  - 农户专用的商品增减、查询接口。
  - 包含作物信息、产地记录。
- **全链路溯源模块 (Tracing Module)**
  - 系统生成并映射唯一“溯源码”(Tracing Code)。
  - C端普通用户的溯源页查询入口。
  - “热门溯源产品”的统计推荐。
- **物流追踪模块 (Logistics Module)**
  - 物流节点状态信息的上链/登记。

## 2. 角色权限矩阵 (Role & Permission Matrix)

这是保障系统**不发生权限错乱的基石**。四种角色严格执行“最小权限原则 (PoLP)”：

| 模块/功能 | 普通用户 (USER) | 农户 (FARMER) | 物流管理员 (LOGISTICS) | 系统管理员 (ADMIN) |
| :--- | :---: | :---: | :---: | :---: |
| **登录/注册** | ✅ | ✅ | ✅ | ✅ |
| **溯源查询** | ✅ | ✅ | ✅ | ✅ |
| **查看热门推荐** | ✅ | ✅ | ✅ | ✅ |
| **商品管理(增/删)** | ❌ | ✅ (仅限自己的商品) | ❌ | ✅ (所有商品) |
| **生成溯源码** | ❌ | ✅ (仅限自己的商品) | ❌ | ✅ (所有溯源码)|
| **物流流转管理** | ❌ | ❌ | ✅ | ✅ |
| **全系统用户管理** | ❌ | ❌ | ❌ | ✅ |

## 3. 整体技术实现思路

为了防止越权访问，前端与后端必须实现**双端安全拦截**，以符合现代 Web 开发规范。

### 后端安全实现 (Spring Boot + Spring Security + JWT)
1. **统一认证**：选用 JWT 来实现无状态 Authentication，将 `ROLE` 写入 payload 中。
2. **方法级拦截**：在 Spring Boot 控制层使用 `@PreAuthorize("hasRole('FARMER')")` 之类的注解，严格卡死接口入口。
3. **数据隔离级拦截**：农户删除或修改商品时，SQL / ORM 查询必须带上 `WHERE farmer_id = {当前登录用户ID}`。如果不属于该农户，抛出 `403 Forbidden`。以此避免 A农户删掉 B农户商品 的**水平越权漏洞**。

### 前端体验实现 (Vue 3 + Vue Router)
1. **路由守卫 (Navigation Guard)**：在 `router.beforeEach` 中判断当前 Vuex/Pinia 记录的用户角色，非农户强行访问 `/farmer/product` 会被直接拦截跳转至 `/403`。
2. **UI 元素隐藏 (指令)**：自定义 Vue 指令 `v-permission="['FARMER', 'ADMIN']"`。如在导航栏上，普通用户不会看到“添加商品”和“录入物流”的按钮。避免“看得到点进去却报错”的不良体验。
3. **视觉标准**：基于现代组件库（如 Element Plus / Shadcn UI Vue），通过 Skeleton (骨架屏) 和 Toast 处理异步加载反馈，保障交互流畅度。

---

## 4. 数据库设计 (Database Design)

遵循第三范式 (3NF) 标准设计，且符合 `<user_rule.md>` 的 `utf8mb4`、预置 Seed 数据等所有规则。

已自动为您在根目录生成 `init.sql`（可直接通过 Navicat 导入，或供后续 docker-compose 挂载作为初始化脚本使用）。

### 核心表结构预览：

1. **`sys_user` (系统用户表)**
   - 包含普通用户、农户、物流管理员、系统管理员。
   - 核心字段：`id`, `username`, `password`, `role`。
2. **`product` (农产品信息表)**
   - 核心字段：`id`, `farmer_id` (外键关联用户表), `name`, `category`, `origin`。
3. **`tracing_code` (溯源码表)**
   - 将“生鲜实体”转化为一串唯一识别码。
   - 核心字段：`id`, `product_id` (外键关联商品表), `trace_code` (UNIQUE)。
4. **`logistics` (物流信息表)**
   - 记录各流转节点的运送轨迹。
   - 核心字段：`id`, `trace_code_id`, `admin_id`, `location`, `status_desc`。
5. **`hot_product` (热门溯源产品表)**
   - 优化核心业务查询效率的分离表。
   - 核心字段：`id`, `product_id`, `search_count`。
