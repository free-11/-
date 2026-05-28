# 中午吃什么 - 全栈应用

一个帮助你决定"中午吃什么"的全栈应用，包含 Android 客户端和 Spring Boot 后端。

## 项目结构

```
projects/
├── app/                    # Android 客户端
├── backend/                # Spring Boot 后端
└── README.md
```

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- MySQL
- Java 17

### Android
- Kotlin
- Jetpack Compose
- Material 3
- Retrofit 2
- ViewModel

## 功能特性

1. **用户认证**
   - 用户注册
   - 用户登录
   - 测试账号: test@example.com / 123456

2. **午餐抽签**
   - 点击按钮随机抽取午餐
   - 带有旋转动画效果
   - 显示午餐名称和描述

3. **午餐管理**
   - 添加新午餐选项
   - 编辑现有午餐
   - 删除午餐选项
   - 支持标签功能

## 快速开始

### 后端启动

1. 确保已安装 Java 17+ 和 MySQL
2. 创建数据库并执行初始化脚本:
   ```sql
   source backend/src/main/resources/schema.sql
   ```
3. 修改 `backend/src/main/resources/application.yml` 中的数据库连接信息:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/what_to_eat
       username: your_username
       password: your_password
   ```
4. 运行后端应用:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```
   后端将在 `http://localhost:8080` 启动

### Android 启动

1. 确保已安装 Android Studio
2. 打开 `app` 目录
3. 同步 Gradle 依赖
4. 运行应用到模拟器或真机

**注意**: 如果使用真机，需要修改 `RetrofitClient.kt` 中的服务器地址为电脑的局域网 IP

## API 接口

### 用户接口
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/{id}` - 获取用户信息

### 午餐接口
- `GET /api/lunch/list/{userId}` - 获取午餐列表
- `GET /api/lunch/random/{userId}` - 随机抽取午餐
- `POST /api/lunch` - 添加午餐
- `PUT /api/lunch/{id}` - 更新午餐
- `DELETE /api/lunch/{id}` - 删除午餐

## 预置数据

应用启动时会自动创建:
- 测试用户: test@example.com (密码: 123456)
- 预置午餐: 麻辣烫、炸鸡、寿司、汉堡、酸菜鱼、沙拉
