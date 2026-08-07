# ADR-0007: Map Provider Adapter

Date: 2026-08-07

Status: Accepted

## Decision

采用 Provider Adapter Pattern 抽象地图能力，MVP 阶段不绑定具体地图供应商（高德 / 腾讯 / 百度）。

定义 `MapProviderAdapter` 接口（`common/adapter/` 包），封装以下能力：
- `geocode(address)` — 地址 → 坐标
- `reverseGeocode(lat, lng)` — 坐标 → 地址
- `searchNearby(lat, lng, radius, type)` — 附近搜索

Sprint 3 提供 `MockMapProviderAdapter`（静态数据），Sprint 5+ 接入真实 SDK 实现。

## Reason

- **业务驱动**：SPRINT_PLAN Sprint 3 Explore Module 需要地图搜索 / 逆地理编码能力，但 MVP 阶段选型未定
- **技术约束**：ARCHITECTURE §18 要求外部调用通过 Adapter 层，实现可替换（换地图供应商不影响业务）
- **团队约束**：避免 MVP 阶段引入地图 SDK 依赖（高德 / 腾讯需申请 Key + SDK 集成），先用 Mock 跑通业务闭环
- **与 ADR-0005 一致**：Vector DB Adapter 同样采用延迟绑定策略，Sprint 3 地图 Adapter 遵循相同模式

## Impact

- **新增接口**：`common/adapter/MapProviderAdapter.java`（接口定义）
- **Mock 实现**：`common/adapter/MockMapProviderAdapter.java`（静态数据，Sprint 5 替换）
- **影响模块**：Explore Module（LocationApplicationService 调用 Adapter 做附近搜索）
- **不影响现有数据**：纯新增接口，不修改已有表 / Entity
- **配置预留**：application.yml 增加 `map.provider` 配置项（Sprint 5+ 填入真实 Key）

## Migration / Follow-up

- **Sprint 5+ Follow-up**：接入真实地图 SDK（高德 / 腾讯），实现 `GaodeMapProviderAdapter` 或 `TencentMapProviderAdapter`，通过 `@ConditionalOnProperty` 切换
- **验证方式**：MockMapProviderAdapter 返回静态 Location 列表，前端可展示卡片流
