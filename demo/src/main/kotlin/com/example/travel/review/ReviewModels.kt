package com.example.travel.review

import kotlinx.serialization.Serializable

/** Published when a trip is submitted - picked up by [ReviewJob] after commit. */
data class TripSubmitted(val tripId: Long)

@Serializable
enum class Verdict { APPROVED, NEEDS_CHANGES, REJECTED }

/** What the agent has to produce. */
@Serializable
data class ReviewOutcome(
    val verdict: Verdict,
    val summary: String,
    val violations: List<String> = emptyList(),
)

/** The judge's opinion about a draft review. */
@Serializable
data class Judgement(
    val accepted: Boolean,
    val feedback: String,
)
