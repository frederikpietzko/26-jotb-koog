# Travel policy demo - live coding a Koog agent

A Kotlin + Spring Boot 4 travel service: business **trips**
(hotel, flight and train bookings) and plain-text **travel policies**. Submitting a trip queues an
asynchronous review - the reviewer itself is written live with Koog during the talk.

What is already there:

- `trip/`, `policy/` - domain, REST API and `TripService`
- `review/TravelFacts.kt` - trip snapshot, policies and cost breakdown, ready to expose as tools
- `review/Prompts.kt` - reviewer and judge prompts
- `review/ReviewJob.kt` - runs `TripReviewAgent` after submit and stores the outcome
- `review/KoogConfig.kt` - Koog `PromptExecutor` backed by Spring AI's `ChatModel`

What is missing: the body of `review/TripReviewAgent.kt`. Until it is written, a submitted trip ends up as
`REVIEW_FAILED`.

## Versions

Spring Boot 4.1.1, Spring AI 2.1.0-M1 (Responses API), Koog 1.3.0
(`koog-spring-ai-v2-starter-model-chat`), Kotlin 2.4.20, Java 25.

## Run

Credentials (`OPENAI_BASE_URL`, `OPENAI_API_KEY`) are read from `.env`.

```bash
./gradlew bootRun
```

Two trips are seeded: trip `1` complies with every policy, trip `2` breaks several. See `requests.http`.

## Observability - Koog traces in Langfuse

`docker-compose.yml` is Langfuse's own [compose file](https://github.com/langfuse/langfuse/blob/main/docker-compose.yml)
with the headless-init variables pre-filled, so org, project, API keys and login exist right after startup:

```bash
docker compose up -d        # first start takes a minute - wait for http://localhost:3000
```

Login: `demo@jotb.local` / `jotb-demo` - project **Travel Policy Demo**.

`TripReviewAgent` already gets `LangfuseProperties` (`koog.langfuse.*`) injected. To paste into the agent builder:

```kotlin
AIAgent(promptExecutor = executor, strategy = ..., agentConfig = ..., toolRegistry = ...) {
    if (langfuse.enabled) install(OpenTelemetry) {
        setServiceInfo("koog-travel-policy-demo", "0.0.1")
        setVerbose(langfuse.verbose)
        // one SDK per agent run - flush and shut it down when the run ends
        setShutdownOnAgentClose(true)
        addLangfuseExporter(langfuse.host, langfuse.publicKey, langfuse.secretKey)
    }
}
```

Imports: `ai.koog.agents.features.opentelemetry.feature.OpenTelemetry` and
`ai.koog.agents.features.opentelemetry.integration.langfuse.addLangfuseExporter`.

Host and keys default to the compose values, so the flag is all you need. System properties to paste:

```bash
# Gradle - koog.* system properties are forwarded to the app
./gradlew bootRun -Dkoog.langfuse.enabled=true

# everything explicit (IntelliJ run configuration -> VM options)
-Dkoog.langfuse.enabled=true -Dkoog.langfuse.host=http://localhost:3000 -Dkoog.langfuse.public-key=pk-lf-jotb-demo -Dkoog.langfuse.secret-key=sk-lf-jotb-demo -Dkoog.langfuse.verbose=true
```

Then `curl -X POST localhost:8080/api/trips/2/submit` and open Tracing in Langfuse: one trace per review, with spans
for every node, LLM call (prompt, completion, tokens) and tool call. `koog.langfuse.verbose=false` keeps prompt and
completion contents out of the spans.

`docker compose down -v` removes Langfuse together with its data.

## Tests

```bash
./gradlew test
```

`ScriptedChatModel` (`src/test/kotlin/.../fakes`) replaces the LLM for tests of the agent once it is written.
