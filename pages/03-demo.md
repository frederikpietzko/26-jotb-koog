---
layout: intro
class: section-intro
---

# The demo

## one boring business process: reviewing business trips

<!--
- Travel expense approval - everyone has it, nobody likes it
- Plain Spring Boot 4 service, already running
- Only the reviewer is missing - built live with Koog
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-books />context</div>

# A trip is a plain JPA aggregate with its bookings

```kotlin no-compile
@Entity
class Trip(
    var traveler: String,
    var destination: String,
    var startDate: LocalDate,
    var status: TripStatus = TripStatus.DRAFT,
    @OneToMany(cascade = [CascadeType.ALL])
    val hotels: MutableSet<HotelBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL])
    val flights: MutableSet<FlightBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL])
    val trains: MutableSet<TrainBooking> = linkedSetOf(),
)
```

<!--
- Spring Data JPA, REST, H2
- Hotels, flights, trains
  - prices, dates, classes - facts to check
- Trimmed: purpose, origin, end date, submit timestamp
- Context - data already lives here
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-books />context</div>

# The rules are sentences written for people, not for code

```kotlin no-compile
Policy("Hotel budget",
    "Hotels may cost at most 180 EUR per night. " +
    "In London, Paris and Zurich the limit is 250 EUR per night.")

Policy("Rail first",
    "If the train journey takes 4 hours or less, the train must be used " +
    "instead of a flight. Second class only.")
```

<!--
- Five policies: hotel budget, flight class, rail first, lead time, weekend stays
- Why a model
  - prose by HR, full of exceptions
  - if-else for all = boring work
- Policies = table rows
  - new policy at runtime, no redeploy
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-plugs-connected />integration</div>

# The agent reads the facts through one read-only service

```kotlin no-compile
@Service
@Transactional(readOnly = true)
class TravelFacts(private val trips: TripRepository, private val policies: PolicyRepository) {
    fun trip(tripId: Long): TripSnapshot
    fun policies(): List<PolicyText>
    fun costs(tripId: Long): CostBreakdown
}
```

<!--
- Becomes the agent's tools
  - logic here, not in framework code
- readOnly = true
  - agent looks, never changes
  - job writes the result, not the model
- Snapshots, not entities
  - no lazy loading, no Hibernate proxies in prompts
- Costs computed in code
  - models bad at arithmetic
-->

---
class: code-slide
---

<div class="pillar-tag"><ph-brackets-curly />type safety</div>

# The agent has to answer with a type, not a chat message

```kotlin no-compile
@Serializable
data class ReviewOutcome(
    val verdict: Verdict,
    val summary: String,
    val violations: List<String> = emptyList(),
)

@Serializable
data class Judgement(val accepted: Boolean, val feedback: String)
```

<!--
- ReviewOutcome - what the reviewer produces
  - Verdict enum: APPROVED, NEEDS_CHANGES, REJECTED
  - maps to trip status
- Judgement - what the judge produces
- Both serializable - Koog derives the JSON Schema
-->

---
class: code-slide code-dense
---

<div class="pillar-tag"><ph-lifebuoy />reliability</div>

# Submitting a trip queues the review after the commit

```http
POST /api/trips/2/submit
```

```kotlin no-compile
@Async
@TransactionalEventListener
fun on(event: TripSubmitted) {
    val review = agent.review(event.tripId)
    reviews.save(TripReview(event.tripId, review))
}
```

<!--
- Submit publishes TripSubmitted, returns 202
- Review after commit, on another thread
  - plain Spring
  - no DB transaction held while waiting on the model
- Simplified: IN_REVIEW, duration, REVIEW_FAILED
- Seeded trips
  - trip 1: Zagreb - Ljubljana by train, compliant
  - trip 2: Vienna - Munich, business class 1h flight, 520 EUR hotel, 5 days ahead
-->

---
class: code-slide
---

# The only thing missing is the agent

```kotlin no-compile
@Component
class TripReviewAgent(
    private val executor: PromptExecutor,
    private val facts: TravelFacts,
) {
    fun review(tripId: Long): ReviewOutcome = TODO("Build the Koog agent")
}
```

<!--
- PromptExecutor - Koog, backed by Spring AI ChatModel
- TravelFacts - injected like any bean
- Until written: every trip ends as REVIEW_FAILED
-->

---
class: graphic-slide
---

# The reviewer drafts, a judge audits, a rejection goes back

<div class="flow flow-5">
  <div class="flow-node"><ph-robot /><span>reviewer with tools</span></div>
  <div class="flow-arrow"><span>draft</span></div>
  <div class="flow-node"><ph-gavel /><span>judge without tools</span></div>
  <div class="flow-arrow"><span>accepted</span></div>
  <div class="flow-node"><ph-seal-check /><span>ReviewOutcome</span></div>
  <div v-click class="flow-loop reveal"><span>feedback</span></div>
</div>

<!--
- Judge pattern
  - reviewer with tools writes a draft
  - independent call audits it against the facts
- Why
  - LLMs confident when wrong
  - fresh context catches many mistakes
  - cheap insurance - reliability
- [click] Rejection - feedback back to the reviewer
  - bounded number of revisions
- This graph = what we build in Koog
-->
