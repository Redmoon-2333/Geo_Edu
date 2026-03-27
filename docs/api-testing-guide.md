# GeoEdu API 接口测试完整流程

## 前置条件

### 1. 启动依赖服务

```powershell
docker-compose up -d postgres redis
```

### 2. 启动应用

```powershell
mvn spring-boot:run
```

### 3. 确认服务运行

```bash
curl http://localhost:8080/api/v1/admin/health
```

---

## Apifox 配置

### 环境配置

| 配置项 | 值 |
|--------|-----|
| 环境名称 | 本地开发 |
| 变量名 | `baseUrl` |
| 变量值 | `http://localhost:8080` |

### 认证配置

在集合设置中添加前置脚本（自动获取 Token）：

```javascript
// 登录接口后执行
if (pm.response.code === 0 && pm.response.data.token) {
    pm.collectionVariables.set("token", pm.response.data.token);
}
```

或在环境变量中手动设置：
- 变量名：`token`
- 变量值：`mock-jwt-token`（登录接口返回的 mock 值）

---

## 测试数据

### 数据库初始化

应用启动时自动执行 `init.sql`，包含以下测试数据：

| 表名 | 数据量 | 说明 |
|------|--------|------|
| users | 5条 | admin, teacher, teacher2, student, student2 |
| knowledge | 15条 | 高一地理四章内容 |
| question | 10条 | 关联知识的题目 |
| image | 5条 | 知识配图 |
| chat_log | 4条 | 历史聊天记录 |

---

## 接口测试顺序

### Phase 1: 健康检查与认证

---

#### 1. 健康检查

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `GET` |
| URL | `{{baseUrl}}/api/v1/admin/health` |
| 认证 | 无 |

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "UP",
    "database": "UP",
    "redis": "UP",
    "ollama": "UP"
  }
}
```

---

#### 2. 登录

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `POST` |
| URL | `{{baseUrl}}/api/v1/auth/login` |
| Headers | `Content-Type: application/json` |
| Body | `raw JSON` |
| 认证 | 无 |

**请求 Body**：
```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "mock-jwt-token",
    "expiresIn": 86400,
    "user": {
      "username": "testuser",
      "role": "student"
    }
  }
}
```

> **注意**：当前登录是 Mock 实现，任意用户名密码都可登录。

---

#### 3. 注册

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `POST` |
| URL | `{{baseUrl}}/api/v1/auth/register` |
| Headers | `Content-Type: application/json` |
| Body | `raw JSON` |
| 认证 | 无 |

**请求 Body**：
```json
{
  "username": "newuser",
  "password": "password123"
}
```

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "mock-jwt-token",
    "expiresIn": 86400,
    "user": {
      "username": "newuser",
      "role": "student"
    }
  }
}
```

---

### Phase 2: 知识库操作

> 以下接口需要在 Header 中添加：`Authorization: Bearer {{token}}`

---

#### 4. 获取知识列表

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `GET` |
| URL | `{{baseUrl}}/api/v1/knowledge` |
| Headers | `Authorization: Bearer {{token}}` |
| Query | `page=0`, `size=10` |
| 认证 | 需要 |

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "K001",
        "title": "地球的内部结构",
        "content": "地球的内部结构可以分为三层...",
        "difficulty": "easy",
        "grade": "高一",
        "chapter": "第一章 行星地球"
      }
    ],
    "total": 15,
    "page": 0,
    "size": 10,
    "pages": 2
  }
}
```

---

#### 5. 按条件筛选知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `GET` |
| URL | `{{baseUrl}}/api/v1/knowledge` |
| Headers | `Authorization: Bearer {{token}}` |
| Query | `grade=高一`, `chapter=第一章 行星地球`, `difficulty=easy` |

**筛选条件说明**：
- `grade`：年级，如"高一"、"高二"
- `chapter`：章节，如"第一章 行星地球"
- `difficulty`：难度，取值 `easy`、`medium`、`hard`

---

#### 6. 搜索知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `GET` |
| URL | `{{baseUrl}}/api/v1/knowledge/search` |
| Headers | `Authorization: Bearer {{token}}` |
| Query | `keyword=地球`, `page=0`, `size=10` |

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "K001",
        "title": "地球的内部结构",
        "content": "地球的内部结构可以分为三层：地壳、地幔和地核...",
        "difficulty": "easy",
        "grade": "高一",
        "chapter": "第一章 行星地球"
      }
    ],
    "total": 5,
    "page": 0,
    "size": 10,
    "pages": 1
  }
}
```

---

#### 7. 获取单条知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `GET` |
| URL | `{{baseUrl}}/api/v1/knowledge/{id}` |
| Headers | `Authorization: Bearer {{token}}` |
| Path | `id=K001` |

**测试 ID**：
- `K001` ~ `K005`：第一章 行星地球
- `K006` ~ `K009`：第二章 地球上的大气
- `K010` ~ `K012`：第三章 地球上的水
- `K013` ~ `K015`：第四章 地表形态的塑造

---

#### 8. 创建知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `POST` |
| URL | `{{baseUrl}}/api/v1/knowledge` |
| Headers | `Authorization: Bearer {{token}}`<br>`Content-Type: application/json` |
| Body | `raw JSON` |
| 权限 | 需要 `TEACHER` 或 `ADMIN` 角色 |

**请求 Body**：
```json
{
  "id": "K100",
  "title": "测试知识标题",
  "content": "这是测试知识的内容，包含地理相关的知识点...",
  "difficulty": "medium",
  "grade": "高一",
  "chapter": "第一章 行星地球",
  "pageNumber": 99
}
```

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "K100",
    "title": "测试知识标题",
    "content": "这是测试知识的内容...",
    "difficulty": "medium",
    "grade": "高一",
    "chapter": "第一章 行星地球",
    "pageNumber": 99
  }
}
```

---

#### 9. 更新知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `PUT` |
| URL | `{{baseUrl}}/api/v1/knowledge/{id}` |
| Headers | `Authorization: Bearer {{token}}`<br>`Content-Type: application/json` |
| Path | `id=K100` |
| Body | `raw JSON` |
| 权限 | 需要 `TEACHER` 或 `ADMIN` 角色 |

**请求 Body**：
```json
{
  "title": "更新后的标题",
  "content": "更新后的内容...",
  "difficulty": "hard",
  "grade": "高一",
  "chapter": "第一章 行星地球",
  "pageNumber": 100
}
```

---

#### 10. 删除知识

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `DELETE` |
| URL | `{{baseUrl}}/api/v1/knowledge/{id}` |
| Headers | `Authorization: Bearer {{token}}` |
| Path | `id=K100` |
| 权限 | 需要 `ADMIN` 角色 |

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": null
}
```

---

### Phase 3: 聊天功能

---

#### 11. 发送聊天消息

| 配置项 | 值 |
|--------|-----|
| 请求方法 | `POST` |
| URL | `{{baseUrl}}/api/v1/chat` |
| Headers | `Authorization: Bearer {{token}}`<br>`Content-Type: application/json` |
| Body | `raw JSON` |
| 认证 | 需要 |

**请求 Body**：
```json
{
  "question": "地球的结构是什么？",
  "topK": 3
}
```

**响应预期**：
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "这是一个模拟回答，实际需要集成ChatService来处理",
    "images": [],
    "relatedKnowledge": [],
    "relatedQuestions": []
  }
}
```

> **注意**：当前 ChatController 返回的是模拟回答，实际的 RAG 流程需要在应用启动 Ollama 服务后才会真正执行。

---

## 测试知识库数据

### 高一地理 - 第一章 行星地球

| ID | 标题 | 难度 |
|----|------|------|
| K001 | 地球的内部结构 | easy |
| K002 | 地球的外部圈层结构 | easy |
| K003 | 地球的宇宙环境 | medium |
| K004 | 地球的自转和公转 | medium |

### 高一地理 - 第二章 地球上的大气

| ID | 标题 | 难度 |
|----|------|------|
| K005 | 大气的组成和结构 | easy |
| K006 | 大气的热力作用 | medium |
| K007 | 热力环流 | medium |
| K008 | 三圈环流 | hard |

### 高一地理 - 第三章 地球上的水

| ID | 标题 | 难度 |
|----|------|------|
| K009 | 水循环 | easy |
| K010 | 海水运动 | medium |
| K011 | 水资源的合理利用 | medium |

### 高一地理 - 第四章 地表形态的塑造

| ID | 标题 | 难度 |
|----|------|------|
| K012 | 内力作用与地表形态 | easy |
| K013 | 外力作用与地表形态 | medium |
| K014 | 板块构造学说 | hard |
| K015 | 主要地貌类型 | medium |

---

## curl 测试脚本

```powershell
# ===== Phase 1: 健康检查与认证 =====

echo === 1. 健康检查 ===
curl http://localhost:8080/api/v1/admin/health

echo.
echo === 2. 登录 ===
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test\",\"password\":\"123\"}"

# ===== Phase 2: 知识库操作 =====

echo.
echo === 3. 获取知识列表 ===
curl http://localhost:8080/api/v1/knowledge ^
  -H "Authorization: Bearer mock-jwt-token"

echo.
echo === 4. 按章节筛选 ===
curl "http://localhost:8080/api/v1/knowledge?grade=高一&chapter=第一章 行星地球" ^
  -H "Authorization: Bearer mock-jwt-token"

echo.
echo === 5. 搜索知识 ===
curl "http://localhost:8080/api/v1/knowledge/search?keyword=地球" ^
  -H "Authorization: Bearer mock-jwt-token"

echo.
echo === 6. 获取单条知识 ===
curl http://localhost:8080/api/v1/knowledge/K001 ^
  -H "Authorization: Bearer mock-jwt-token"

echo.
echo === 7. 创建知识 ===
curl -X POST http://localhost:8080/api/v1/knowledge ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer mock-jwt-token" ^
  -d "{\"id\":\"KTEST\",\"title\":\"测试知识\",\"content\":\"测试内容\",\"difficulty\":\"easy\",\"grade\":\"高一\",\"chapter\":\"第一章\"}"

echo.
echo === 8. 更新知识 ===
curl -X PUT http://localhost:8080/api/v1/knowledge/KTEST ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer mock-jwt-token" ^
  -d "{\"title\":\"更新标题\",\"content\":\"更新内容\",\"difficulty\":\"medium\",\"grade\":\"高一\",\"chapter\":\"第一章\"}"

# ===== Phase 3: 聊天功能 =====

echo.
echo === 9. 发送聊天 ===
curl -X POST http://localhost:8080/api/v1/chat ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer mock-jwt-token" ^
  -d "{\"question\":\"地球的结构是什么？\",\"topK\":3}"

echo.
echo === 测试完成 ===
```

---

## 常见问题排查

| 现象 | 原因 | 解决方案 |
|------|------|----------|
| 401 Unauthorized | 未携带 Token | 添加 `Authorization: Bearer {{token}}` Header |
| 403 Forbidden | 权限不足 | 创建/更新/删除需要 Teacher 或 Admin 角色 |
| 500 Internal Server Error | 数据库未连接 | 检查 Docker 服务 `docker-compose ps` |
| Connection refused | 服务未启动 | 启动应用 `mvn spring-boot:run` |
| 知识列表为空 | 数据未初始化 | 重启应用自动执行 init.sql |

---

## 后续测试建议

1. **数据库连接测试**：使用 IDEA Database 工具连接 `localhost:5432`
2. **Redis 连接测试**：访问 `http://localhost:8001` 使用 Redis Insight
3. **Ollama 服务测试**：启动 Ollama 容器后测试向量化功能
