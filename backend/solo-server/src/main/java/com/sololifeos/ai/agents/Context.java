package com.sololifeos.ai.agents;

import java.util.Map;

/**
 * Agent 执行上下文。
 *
 * 由 Context Builder 组装，包含用户当前状态信息。
 * 字段可按需扩展，当前仅定义基础结构。
 */
public class Context {

    private Long userId;
    private Map<String, Object> attributes;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
