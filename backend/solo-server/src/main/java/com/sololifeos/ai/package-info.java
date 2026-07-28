/**
 * AI Platform module. Owns Memory and Conversation. Provides the Agent
 * Router, agents, memory layer and LLM provider abstraction. All agents go
 * through the Router and may only mutate business data via Domain API
 * (ARCHITECTURE §7 / §21).
 *
 * <p>Subpackages:
 * <ul>
 *   <li>{@code orchestrator} - Agent routing</li>
 *   <li>{@code agents} - individual agent implementations</li>
 *   <li>{@code memory} - long-term memory &amp; conversation context</li>
 *   <li>{@code llm} - LLM provider &amp; vector store adapters</li>
 * </ul>
 */
package com.sololifeos.ai;
