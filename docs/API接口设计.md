# GeoEdu API 接口设计

> 本文档详细定义 GeoEdu 系统的 RESTful API 接口规范，包括认证机制、Endpoints、请求响应格式及错误码。

## 1. API 规范说明

### 1.1 基本规范

| 项目 | 规范 |
|------|------|
| 协议 | HTTP/HTTPS |
| 编码 | UTF-8 |
| Content-Type | application/json |
| 认证方式 | JWT Bearer Token |
| API 版本 | URL 路径版本控制 /api/v1/ |

### 1.2 请求格式

```http
POST /api/v1/chat HTTP/1.1
Host: api.geoedu.local
Content-Type: application/json
Authorization: Bearer <token>
```

### 1.3 响应格式

**成功响应**：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1711545600
}
```

**错误响应**：

```json
{
  "code": 40001,
  "message": "用户名已存在",
  "errors": [
    {
      "field": "username",
      "message": "重复"
    }
  ],
  "timestamp": 1711545600
}
```

---

## 2. 认证授权机制

### 2.1 JWT Token 结构

| 字段 | 说明 |
|------|------|
| header | 算法信息（HS256） |
| payload | 用户ID、角色、过期时间 |
| signature | 签名验证 |

### 2.2 角色权限矩阵

| 接口 | student | teacher | admin |
|------|---------|---------|-------|
| POST /chat | ✅ | ✅ | ✅ |
| GET /knowledge | ✅ | ✅ | ✅ |
| POST /knowledge | ❌ | ✅ | ✅ |
| PUT /knowledge/{id} | ❌ | ✅ | ✅ |
| DELETE /knowledge/{id} | ❌ | ❌ | ✅ |
| POST /image/upload | ❌ | ✅ | ✅ |
| GET /chat/history | ✅ | ✅ | ✅ |
| POST /admin/backup | ❌ | ❌ | ✅ |

---

## 3. Endpoints 清单

### 3.1 问答接口

#### 3.1.1 智能问答

```http
POST /api/v1/chat
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| question | String | 是 | 用户问题，最大200字 |
| top_k | Integer | 否 | 检索结果数量，默认3 |

**请求示例**：

```json
{
  "question": "地转偏向力是如何产生的？",
  "top_k": 3
}
```

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "answer": "地转偏向力是...",
    "images": [
      {
        "path": "/images/G1-1-01_1.jpg",
        "caption": "地转偏向力示意图"
      }
    ],
    "related_knowledge": [
      {
        "id": "G1-1-01",
        "title": "地球自转的地理意义"
      }
    ],
    "related_questions": [
      {
        "id": "Q1",
        "question": "地转偏向力的方向如何判断？",
        "answer": "北半球向右偏..."
      }
    ]
  },
  "timestamp": 1711545600
}
```

---

### 3.2 知识库接口

#### 3.2.1 创建知识点

```http
POST /api/v1/knowledge
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | String | 是 | 知识点ID，格式：G{年级}-{章节}-{序号} |
| title | String | 是 | 标题，最大200字 |
| content | String | 是 | 正文内容 |
| difficulty | String | 否 | 难度：easy/medium/hard，默认medium |
| page_number | Integer | 否 | 教材页码 |
| grade | String | 否 | 年级，如 G1 |
| chapter | String | 否 | 章节号 |

**请求示例**：

```json
{
  "id": "G1-1-01",
  "title": "地球自转的地理意义",
  "content": "地球自转导致昼夜交替...",
  "difficulty": "medium",
  "page_number": 10,
  "grade": "G1",
  "chapter": "1"
}
```

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "G1-1-01",
    "embedding_status": "completed"
  },
  "timestamp": 1711545600
}
```

#### 3.2.2 获取知识点详情

```http
GET /api/v1/knowledge/{id}
```

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "G1-1-01",
    "title": "地球自转的地理意义",
    "content": "地球自转导致昼夜交替...",
    "difficulty": "medium",
    "page_number": 10,
    "grade": "G1",
    "chapter": "1",
    "images": [
      {
        "id": "IMG001",
        "path": "/images/G1-1-01_1.jpg",
        "caption": "地球自转示意"
      }
    ],
    "created_at": "2026-03-28T10:00:00Z",
    "updated_at": "2026-03-28T10:00:00Z"
  },
  "timestamp": 1711545600
}
```

#### 3.2.3 知识点列表查询

```http
GET /api/v1/knowledge
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20，最大100 |
| grade | String | 否 | 按年级筛选 |
| chapter | String | 否 | 按章节筛选 |
| difficulty | String | 否 | 按难度筛选 |
| keyword | String | 否 | 关键词搜索 |

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "items": [...],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "timestamp": 1711545600
}
```

#### 3.2.4 更新知识点

```http
PUT /api/v1/knowledge/{id}
```

**请求示例**：

```json
{
  "title": "地球自转的地理意义（修订版）",
  "content": "修改后的内容...",
  "difficulty": "hard"
}
```

#### 3.2.5 删除知识点

```http
DELETE /api/v1/knowledge/{id}
```

#### 3.2.6 批量导入知识点

```http
POST /api/v1/knowledge/batch
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel/JSON 文件，最大10MB |
| mode | String | 否 | 导入模式：upsert/update，默认upsert |

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 100,
    "success": 98,
    "failed": 2,
    "errors": [
      {
        "row": 15,
        "id": "G1-1-15",
        "error": "ID格式错误"
      }
    ]
  },
  "timestamp": 1711545600
}
```

---

### 3.3 题目接口

#### 3.3.1 创建题目

```http
POST /api/v1/question
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| knowledge_id | String | 是 | 关联知识点ID |
| question | String | 是 | 题干 |
| answer | String | 是 | 答案 |
| type | String | 否 | 题目类型，默认default |

#### 3.3.2 批量导入题目

```http
POST /api/v1/question/batch
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | Excel/JSON 文件 |

---

### 3.4 图片接口

#### 3.4.1 上传图片

```http
POST /api/v1/image/upload
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 图片文件，支持 JPEG/PNG，最大5MB |
| knowledge_id | String | 否 | 关联知识点ID |

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "IMG20260328123456a",
    "path": "/images/IMG20260328123456a.jpg",
    "original_name": "示意图.jpg",
    "file_size": 102400
  },
  "timestamp": 1711545600
}
```

#### 3.4.2 绑定图片到知识点

```http
POST /api/v1/knowledge/{id}/images
```

```json
{
  "image_id": "IMG001",
  "display_order": 1
}
```

---

### 3.5 问答历史接口

#### 3.5.1 获取历史记录

```http
GET /api/v1/chat/history
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页数量，默认20 |

#### 3.5.2 获取会话详情

```http
GET /api/v1/chat/history/{session_id}
```

---

### 3.6 用户接口

#### 3.6.1 用户登录

```http
POST /api/v1/auth/login
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expires_in": 86400,
    "user": {
      "id": "U001",
      "username": "teacher01",
      "role": "teacher"
    }
  },
  "timestamp": 1711545600
}
```

#### 3.6.2 用户注册

```http
POST /api/v1/auth/register
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，4-20字符 |
| password | String | 是 | 密码，6-20字符 |
| role | String | 否 | 角色，默认student |

---

### 3.7 管理接口

#### 3.7.1 数据备份

```http
POST /api/v1/admin/backup
```

#### 3.7.2 系统健康检查

```http
GET /api/v1/admin/health
```

**响应示例**：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "status": "healthy",
    "version": "1.0.0",
    "uptime": 3600000,
    "components": {
      "database": "up",
      "redis": "up",
      "ollama": "up"
    }
  },
  "timestamp": 1711545600
}
```

---

## 4. 错误码规范

### 4.1 错误码分类

| 错误码区间 | 类别 | 说明 |
|-----------|------|------|
| 0 | 成功 | 请求成功 |
| 1xxxx | 通用错误 | 系统级错误 |
| 2xxxx | 参数错误 | 请求参数校验失败 |
| 3xxxx | 认证错误 | 登录、权限相关 |
| 4xxxx | 业务错误 | 业务逻辑错误 |
| 5xxxx | 外部错误 | AI服务、存储服务 |

### 4.2 详细错误码

| 错误码 | 消息 | 说明 |
|--------|------|------|
| 0 | success | 成功 |
| 10001 | Internal server error | 服务器内部错误 |
| 10002 | Service unavailable | 服务不可用 |
| 20001 | Invalid parameter | 参数格式错误 |
| 20002 | Missing required parameter | 缺少必填参数 |
| 20003 | Parameter out of range | 参数超出范围 |
| 30001 | Unauthorized | 未登录 |
| 30002 | Token expired | Token 过期 |
| 30003 | Permission denied | 权限不足 |
| 40001 | Resource not found | 资源不存在 |
| 40002 | Duplicate resource | 资源已存在 |
| 40003 | Invalid file format | 文件格式错误 |
| 50001 | AI service error | AI 服务调用失败 |
| 50002 | Vector service error | 向量服务异常 |

---

## 5. 公共错误响应

| 场景 | 响应 |
|------|------|
| 参数为空 | `{"code": 20002, "message": "缺少必填参数: question"}` |
| Token无效 | `{"code": 30001, "message": "Unauthorized"}` |
| 权限不足 | `{"code": 30003, "message": "Permission denied"}` |
| 资源不存在 | `{"code": 40001, "message": "知识点不存在"}` |
| AI服务超时 | `{"code": 50001, "message": "AI服务响应超时"}` |

---

## 6. 接口限流

| 端点 | 限制 | 窗口 |
|------|------|------|
| POST /api/v1/chat | 30次 | 每分钟 |
| POST /api/v1/auth/login | 10次 | 每分钟 |
| 其他读接口 | 100次 | 每分钟 |

---

## 7. 参考资料

| 文档 | 说明 |
|------|------|
| [[需求规格说明书.md]] | 功能需求定义 |
| [[技术架构设计.md]] | 技术架构 |
| [[数据库设计.md]] | 数据表结构 |

---

## 8. 环境配置

### 8.1 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| SPRING_DATASOURCE_URL | PostgreSQL 连接地址 | jdbc:postgresql://localhost:5432/geoedu |
| SPRING_DATASOURCE_USERNAME | 数据库用户名 | geoedu |
| SPRING_DATASOURCE_PASSWORD | 数据库密码 | - |
| SPRING_REDIS_URL | Redis 连接地址 | redis://localhost:6379 |
| OLLAMA_BASE_URL | Ollama 服务地址 | http://localhost:11434 |
| OLLAMA_MODEL | 使用的模型名称 | qwen2.5:7b |

### 8.2 配置文件结构

```yaml
# application.yml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver
  data:
    redis:
      url: ${SPRING_REDIS_URL}

ollama:
  base-url: ${OLLAMA_BASE_URL}
  model: ${OLLAMA_MODEL}

app:
  vector:
    dimension: 768
    top-k: 3
  upload:
    path: /data/images
    max-size: 10MB
  auth:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 604800000  # 7天（毫秒）
```

---

> 本文档版本：v1.0
> 生成日期：2026-03-28
> 状态：待补充
