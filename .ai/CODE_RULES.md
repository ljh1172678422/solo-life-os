# 编码规范


# 前端


技术：

Vue3

TypeScript

Pinia


规则：

禁止：

any


必须：

类型定义完整。



组件：

一个组件一个职责。



---

# Java


规范：

Spring Boot


Controller

↓

Service

↓

Repository


禁止：

Controller写业务逻辑。


---

# 数据库


所有表：

必须：

created_time

updated_time


所有删除：

优先软删除。


---

# API


统一返回：


```
{
  code: 0,
  message: "",
  data: {}
}
```



---

# Git


提交格式：

feat:

新增功能


fix:

修复


refactor:

重构
