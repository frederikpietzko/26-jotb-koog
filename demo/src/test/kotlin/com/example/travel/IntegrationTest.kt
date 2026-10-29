package com.example.travel

import com.example.travel.fakes.ScriptedChatModel
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles

/**
 * Full application context with the real Spring AI and Koog wiring -
 * only the LLM itself is swapped for a [ScriptedChatModel].
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
@Import(FakeLlmConfig::class)
annotation class IntegrationTest

@TestConfiguration(proxyBeanMethods = false)
class FakeLlmConfig {
    @Bean
    @Primary
    fun scriptedChatModel() = ScriptedChatModel()
}
