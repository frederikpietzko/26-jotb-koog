package com.example.travel.review

import ai.koog.agents.chatMemory.feature.ChatMemory
import ai.koog.agents.chatMemory.feature.InMemoryChatHistoryProvider
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.builder.subgraph
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.reflect.asTool
import ai.koog.agents.ext.agent.CriticResult
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.agents.ext.agent.subgraphWithVerification
import ai.koog.agents.features.opentelemetry.feature.OpenTelemetry
import ai.koog.agents.snapshot.feature.Persistence
import ai.koog.agents.snapshot.feature.RollbackToolRegistry
import ai.koog.agents.snapshot.providers.InMemoryPersistenceStorageProvider
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.model.PromptExecutor
import com.example.travel.trip.TripRepository
import com.example.travel.trip.TripStatus
import kotlinx.coroutines.runBlocking
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

/** The Koog agent reviewing a submitted trip - built live in the talk. */
@Component
class TripReviewAgent(
    private val executor: PromptExecutor,
    private val facts: TravelFacts,
    private val tripToolset: TripToolset,
    private val langfuse: LangfuseProperties
) {

    fun review(tripId: Long): ReviewOutcome {
        val agent = AIAgent(
            promptExecutor = executor,
            agentConfig = AIAgentConfig(
                model = OpenAIModels.Chat.GPT5_6Luna,
                prompt = prompt("review-prompt") {
                    system(Prompts.REVIEWER)
                },
                maxAgentIterations = 50,
            ),
            toolRegistry = ToolRegistry {
                tools(tripToolset)
            },
            strategy = reviewStrategy(tripId)
        ) {
            install(OpenTelemetry) {
                if (langfuse.enabled) install(OpenTelemetry) {
                    setServiceInfo("koog-travel-policy-demo", "0.0.1")
                    setVerbose(langfuse.verbose)
                    setShutdownOnAgentClose(true)
                    addLangfuseExporter(langfuse.host, langfuse.publicKey, langfuse.secretKey)
                }
            }
            install(Persistence) {
                storage = InMemoryPersistenceStorageProvider()
                enableAutomaticPersistence = true
                rollbackToolRegistry = RollbackToolRegistry {
                    registerRollback(tripToolset::updateTripStatus.asTool(), tripToolset::undoTripStatus.asTool())
                }
            }
            install(ChatMemory) {
                this.chatHistoryProvider = InMemoryChatHistoryProvider()
            }
        }

        return runBlocking {
            agent.run(
                Prompts.review(tripId),
                sessionId = "session-$tripId",
            )
        }
    }

    fun reviewStrategy(tripId: Long) = strategy<String, ReviewOutcome>("review-strategy") {
        val review by subgraphWithTask<String, ReviewOutcome> { it }
        val judge by subgraph<ReviewOutcome, CriticResult<ReviewOutcome>>(freshHistory = true) {
            val verify by subgraphWithVerification<ReviewOutcome>(tools = emptyList()) { outcomeDraft ->
                llm.writeSession {
                    clearHistory()
                    appendPrompt {
                        system(Prompts.JUDGE)
                    }
                }
                Prompts.judge(
                    trip = facts.trip(tripId),
                    policies = facts.policies(),
                    costs = facts.costs(tripId),
                    draft = outcomeDraft
                )
            }
            nodeStart then verify then nodeFinish
        }

        val refine by subgraph<CriticResult<ReviewOutcome>, ReviewOutcome> {
            val prompt by node<CriticResult<ReviewOutcome>, String> { critic ->
                Prompts.revise(
                    tripId = tripId,
                    draft = critic.input,
                    feedback = critic.feedback
                )
            }
            val fixit by nodeLLMRequestStructured<ReviewOutcome>()

            nodeStart then prompt then fixit
            edge(fixit forwardTo nodeFinish transformed { it.getOrThrow().data })
        }


        nodeStart then review then judge
        edge(judge forwardTo refine onCondition { !it.successful })
        edge(refine forwardTo judge)
        edge(judge forwardTo nodeFinish onCondition { it.successful } transformed { it.input })
    }
}

@Component
class TripToolset(
    private val facts: TravelFacts,
    private val tripRepository: TripRepository
) : ToolSet {
    @Tool
    @LLMDescription("get information about the trip")
    fun tripInfo(@LLMDescription("the trip id") tripId: Long) = facts.trip(tripId)

    @Tool
    @LLMDescription("get corporate travel policies")
    fun travelPolicies() = facts.policies()

    @Tool
    @LLMDescription("get trip cost breakdown")
    fun costBreakdown(@LLMDescription("the trip id") tripId: Long) = facts.costs(tripId)

    @Tool
    @LLMDescription("update trip request status")
    fun updateTripStatus(
        @LLMDescription("the trip id")
        tripId: Long,
        @LLMDescription("the current trip status")
        oldStatus: TripStatus,
        @LLMDescription("the new trip status that should override the old one")
        newStatus: TripStatus,
    ) {
        val trip = tripRepository.findByIdOrNull(tripId)!!
        require(trip.status == oldStatus) { "Trip status does not match the old status" }
        trip.status = newStatus
        tripRepository.save(trip)
    }

    @Tool
    @LLMDescription("rollabck trip request")
    fun undoTripStatus(
        @LLMDescription("the trip id")
        tripId: Long,
        @LLMDescription("the old trip status before updateing")
        oldStatus: TripStatus,
        @LLMDescription("the current trip status that should be overriden by the old one")
        newStatus: TripStatus,
    ) {
        val trip = tripRepository.findByIdOrNull(tripId)!!
        require(trip.status == newStatus) { "Trip status does not match the new status" }
        trip.status = oldStatus
        tripRepository.save(trip)
    }

}

