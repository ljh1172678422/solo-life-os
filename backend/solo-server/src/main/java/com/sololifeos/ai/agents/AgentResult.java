package com.sololifeos.ai.agents;

/**
 * Agent 执行结果。
 *
 * Agent 产出不直接落库，由调用方通过 Domain API 持久化。
 */
public class AgentResult {

    private boolean success;
    private String content;
    private int tokenCount;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public static AgentResult success(String content, int tokenCount) {
        AgentResult result = new AgentResult();
        result.success = true;
        result.content = content;
        result.tokenCount = tokenCount;
        return result;
    }

    public static AgentResult failure() {
        AgentResult result = new AgentResult();
        result.success = false;
        return result;
    }
}
