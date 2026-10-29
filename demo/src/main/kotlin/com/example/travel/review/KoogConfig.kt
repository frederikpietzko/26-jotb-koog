package com.example.travel.review

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.spring.ai.chat.SpringAiLLMClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Koog's PromptExecutor backed by Spring AI's ChatModel (koog-spring-ai).
 *
 * The starter would auto-configure this, but it orders itself after the classic
 * OpenAiChatAutoConfiguration only - Spring AI 2.1's Responses model comes from
 * OpenAiResponsesChatAutoConfiguration, so we wire the bridge explicitly.
 */
@Configuration
@EnableConfigurationProperties(LangfuseProperties::class)
class KoogConfig {

    @Bean
    fun koogPromptExecutor(chatModel: ChatModel): PromptExecutor = MultiLLMPromptExecutor(
        SpringAiLLMClient.builder()
            .chatModel(chatModel)
            .provider(LLMProvider.OpenAI)
            .build()
    )
}

/**
 * Koog's OpenTelemetry feature exporting to Langfuse - off unless `koog.langfuse.enabled=true`.
 * Defaults match the local `docker-compose.yml` (headless init project keys).
 */
@ConfigurationProperties("koog.langfuse")
data class LangfuseProperties(
    val enabled: Boolean = false,
    val host: String = "http://localhost:3000",
    val publicKey: String = "pk-lf-jotb-demo",
    val secretKey: String = "sk-lf-jotb-demo",
    // verbose: prompts, completions and tool arguments end up in the spans
    val verbose: Boolean = true,
)
