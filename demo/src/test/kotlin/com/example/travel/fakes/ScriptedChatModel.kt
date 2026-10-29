package com.example.travel.fakes

import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.MessageType
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.model.tool.ToolCallingChatOptions
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Stands in for the LLM behind Koog (which calls it through the koog-spring-ai bridge as a Spring AI [ChatModel]).
 *
 * Prompts whose system message is the judge prompt get the next scripted judgement,
 * all other prompts get the next scripted reviewer answer. When a script runs dry the last entry is repeated.
 *
 * Koog's subgraphs expect the result as a call of their finish tool. Whenever that tool is offered,
 * a scripted JSON answer is sent as that tool call instead.
 */
class ScriptedChatModel : ChatModel {

    private val reviewerReplies = ArrayDeque<AssistantMessage>()
    private val judgeReplies = ArrayDeque<AssistantMessage>()
    private val calls = CopyOnWriteArrayList<Prompt>()

    /** Every prompt the model received, in order. */
    val prompts: List<Prompt> get() = calls.toList()

    val judgePrompts: List<Prompt> get() = calls.filter { it.isJudge() }
    val reviewerPrompts: List<Prompt> get() = calls.filterNot { it.isJudge() }

    @Synchronized
    fun reviewerAnswers(vararg replies: AssistantMessage) = apply { reviewerReplies += replies }

    @Synchronized
    fun reviewerSays(vararg texts: String) = reviewerAnswers(*texts.map(::AssistantMessage).toTypedArray())

    @Synchronized
    fun judgeSays(vararg texts: String) = apply { judgeReplies += texts.map(::AssistantMessage) }

    @Synchronized
    fun reset() {
        reviewerReplies.clear()
        judgeReplies.clear()
        calls.clear()
    }

    /** Like every real tool-capable model (Koog's bridge refuses models without tool options). */
    override fun getDefaultOptions(): ChatOptions = ToolCallingChatOptions.builder().build()
    override fun getOptions(): ChatOptions = defaultOptions

    @Synchronized
    override fun call(prompt: Prompt): ChatResponse {
        calls += prompt
        val script = if (prompt.isJudge()) judgeReplies else reviewerReplies
        val reply = if (script.size > 1) script.removeFirst() else script.firstOrNull()
            ?: error("No scripted reply for prompt: ${prompt.contents.take(200)}")
        return ChatResponse(listOf(Generation(reply.asFinishToolCallIfOffered(prompt))))
    }

    private fun Prompt.isJudge() = instructions.any {
        it.messageType == MessageType.SYSTEM && it.text.orEmpty().contains(JUDGE_MARKER)
    }

    private fun Prompt.offersTool(name: String): Boolean {
        val options = options as? ToolCallingChatOptions ?: return false
        return options.toolCallbacks.orEmpty().any { it.toolDefinition.name() == name }
    }

    private fun AssistantMessage.asFinishToolCallIfOffered(prompt: Prompt): AssistantMessage {
        val json = text.orEmpty().trim()
        if (toolCalls.isNotEmpty() || !json.startsWith("{") || !prompt.offersTool(KOOG_FINISH_TOOL)) return this
        // Koog's verification subgraph calls the verdict isCorrect, Judgement calls it accepted
        val result = if (prompt.isJudge()) json.replace("\"accepted\"", "\"isCorrect\"") else json
        return toolCall(KOOG_FINISH_TOOL, """{"result": $result}""")
    }

    companion object {
        private const val JUDGE_MARKER = "strict senior auditor"
        private const val KOOG_FINISH_TOOL = "finalize_task_result"

        fun toolCall(name: String, arguments: String): AssistantMessage = AssistantMessage.builder()
            .content("")
            .toolCalls(listOf(AssistantMessage.ToolCall("call-$name", "function", name, arguments)))
            .build()
    }
}

object Replies {
    const val APPROVED = """{"verdict":"APPROVED","summary":"All policies are met.","violations":[]}"""
    const val NEEDS_CHANGES =
        """{"verdict":"NEEDS_CHANGES","summary":"Hotel too expensive.","violations":["Hotel budget: 520 EUR exceeds 180 EUR"]}"""
    const val ACCEPT = """{"accepted":true,"feedback":"Correct."}"""
    const val REJECT = """{"accepted":false,"feedback":"You missed the hotel budget violation."}"""
}
