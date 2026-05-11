<p align="center">
  <h1>🍊 飘香水果商城</h1>
  <p><strong>Spring Boot + Vue3 全栈水果电商平台</strong></p>
</p>

---

## 📖 项目简介

飘香水果商城是一个全栈水果电商平台，涵盖**消费者端**、**商家端**、**管理端**三个子系统，支持商品浏览、购物车、在线聊天、优惠券、售后服务、支付宝模拟支付等功能。

> 本项目为计算机相关专业毕业设计作品。

---

## 📸 项目截图

<h3>消费者端</h3>

<p align="center">
  <img src="docs/screenshots/1.1.png" width="400">
  <img src="docs/screenshots/1.2.png" width="400">
</p>

<p align="center">
  <img src="docs/screenshots/1.3.png" width="400">
  <img src="docs/screenshots/1.5.png" width="400">
</p>

<p align="center">
  <img src="docs/screenshots/1.11.png" width="400">
  <img src="docs/screenshots/1.6.png" width="400">
</p>

<h3>商家端</h3>

<p align="center">
  <img src="docs/screenshots/2.1.png" width="400">
  <img src="docs/screenshots/2.2.png" width="400">
  <img src="docs/screenshots/2.3.png" width="400">
</p>

<h3>管理端</h3>

<p align="center">
  <img src="docs/screenshots/微信图片_20260511125711_144_632.png" width="800">
</p>

<p align="center">
  <img src="docs/screenshots/3.1.png" width="400">
  <img src="docs/screenshots/微信图片_20260511125217_140_632.png" width="400">
</p>

<p align="center">
  <img src="docs/screenshots/1.7.png" width="250">
  <img src="docs/screenshots/1.8.png" width="250">
  <img src="docs/screenshots/1.9.png" width="250">
  <img src="docs/screenshots/1.10.png" width="250">
</p>

<p align="center">
  <img src="docs/screenshots/1.4.png" width="250">
  <img src="docs/screenshots/微信图片_20260511125216_138_632.png" width="250">
  <img src="docs/screenshots/微信图片_20260511125217_139_632.png" width="250">
</p>

<p align="center">
  <img src="docs/screenshots/微信图片_20260511125217_141_632.png" width="250">
  <img src="docs/screenshots/微信图片_20260511125217_142_632.png" width="250">
  <img src="docs/screenshots/微信图片_20260511125217_143_632.png" width="250">
  <img src="docs/screenshots/微信图片_20260511125216_137_632.png" width="250">
</p>

---

## ✨ 功能模块

| 模块 | 消费者端 | 商家端 | 管理端 |
|------|----------|--------|--------|
| 首页/看板 | 商品浏览、分类筛选、搜索 | 数据看板（订单、收入图表） | 全平台数据总览 |
| 商品 | 商品列表、详情页 | 上架/下架/编辑商品 | 全平台商品管理 |
| 购物车 | 添加/修改/删除 | — | — |
| 订单 | 下单、支付（支付宝模拟） | 订单查看、发货 | 查看所有订单 |
| 优惠券 | 领取和使用 | 发放店铺优惠券 | 平台优惠券管理 |
| 售后 | 申请退款/退货 | 处理售后申请 | 全平台售后管理 |
| 收藏 | 收藏商品、关注商家 | — | — |
| 聊天 | 与商家实时聊天 | 与消费者聊天 | — |
| 评价 | 发表评价 | 查看/回复评价 | — |
| 公告 | 查看系统公告 | 查看公告 | 发布系统公告 |
| 用户管理 | — | — | 管理消费者账号 |
| 商家管理 | — | — | 审核和管理商家 |
| 分类管理 | — | — | 商品分类维护 |
| 消息管理 | — | — | 查看用户留言 |

---

## 🛠 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.7.14 |
| ORM | MyBatis-Plus | 3.5.3.1 |
| 鉴权 | JWT (jjwt) | 0.11.5 |
| 数据库 | MySQL | 8.0 |
| 前端框架 | Vue 3 | 3.3.4 |
| 构建工具 | Vite | 5.0.8 |
| UI 组件库 | Element Plus | 2.4.2 |
| 状态管理 | Pinia | 2.1.7 |
| 图表 | ECharts | 6.0.0 |

---

## 📐 系统架构

```
┌─────────────────────────────────────────────┐
│               前端 (Vue3 + Vite)              │
│    消费者端        商家端        管理端        │
└──────────────────┬──────────────────────────┘
                   │ HTTP /api
┌──────────────────▼──────────────────────────┐
│           后端 (Spring Boot 2.7)             │
│   Controller → Service → Mapper → MySQL      │
│              JWT 鉴权 + 支付宝SDK             │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│              MySQL 8.0 数据库                 │
│              库名: fruit_shop                 │
└─────────────────────────────────────────────┘
```

---

## 🚀 快速开始

### 环境要求

- JDK 1.8+  ·  MySQL 8.0+  ·  Node.js 16+  ·  Maven 3.6+

### 1. 数据库初始化

```bash
mysql -u root -p < fruit_shop.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
# 运行在 http://localhost:8080/api
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 运行在 http://localhost:3000
```

---

## 📁 项目结构

```
fruit-shop/
├── backend/                    # Spring Boot 后端
│   ├── pom.xml                 # Maven 依赖
│   └── src/main/java/com/fruitshop/
│       ├── common/             # 工具类 (JWT、密码、返回格式)
│       ├── config/             # 配置类 (跨域、拦截器、支付宝)
│       ├── controller/         # 接口控制器 (26个)
│       ├── entity/             # 数据实体
│       ├── interceptor/        # 鉴权拦截器
│       ├── mapper/             # MyBatis-Plus 数据访问层
│       └── service/            # 业务逻辑层
├── frontend/                   # Vue3 前端
│   └── src/
│       ├── views/              # 页面视图 (40+ 页面)
│       │   ├── *.vue           # 消费者端
│       │   ├── admin/          # 管理端
│       │   └── merchant/       # 商家端
│       ├── router/             # 路由 + 三端鉴权守卫
│       ├── stores/             # Pinia 状态管理
│       └── utils/              # API 封装
├── docs/                       # 项目文档与截图
└── fruit_shop.sql              # 数据库初始化脚本
```

---

## License

本项目仅用于学习交流，未经许可不得用于商业用途。
