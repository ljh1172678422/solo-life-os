# Solo Life OS 系统架构


## 总体架构


~~~
                Client

 ┌───────────────┐
 │ uni-app       │
 │ H5/App/小程序 │
 └───────┬───────┘

         │

 API Gateway

         │

 ┌─────────────────────┐

 Spring Boot Services

 ├ User Service
 ├ Plan Service
 ├ Explore Service
 ├ Mood Service
 ├ Growth Service
 ├ Community Service

 └─────────────────────┘


         │


 AI Platform

 ├ Planner Agent
 ├ Recommendation Agent
 ├ Emotion Agent
 ├ Story Agent

         │


 PostgreSQL

 Redis

 Vector DB

~~~


---

# 服务设计

## User Service

负责：

* 用户
* 偏好
* 设置

## Plan Service

负责：

Today模块。

核心：

DailyPlan

## Explore Service

负责：

地点

路线

收藏

## Mood Service

负责：

情绪数据

## Growth Service

负责：

目标成长

## Community Service

负责：

活动社交

---

# AI架构


~~~
User Data

 ↓

Memory Layer

 ↓

Agent Router

 ↓

Specific Agent

 ↓

Response

~~~


---

# Agent列表

## Planner Agent

生成每日计划

输入：

* 时间
* 地点
* 天气
* 心情
* 用户偏好

---

## Recommendation Agent

地点推荐

---

## Emotion Agent

情绪分析

禁止：

心理诊断。

---

## Story Agent

年度人生故事。
