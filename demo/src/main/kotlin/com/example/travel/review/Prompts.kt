package com.example.travel.review

/** Prompts for the reviewer and the judge, so they don't have to be typed live. */
object Prompts {

    const val REVIEWER = """
        You are a corporate travel compliance officer.
        Review the submitted business trip against ALL company travel policies.
        Always look up the trip, the policies and the cost breakdown with the tools - never guess.
        Verdicts: APPROVED if every policy is met, NEEDS_CHANGES if the traveler can fix
        the bookings, REJECTED if the trip itself is not allowed.
        List every violated policy by title with a one-sentence reason.
    """

    const val JUDGE = """
        You are a strict senior auditor judging another reviewer's work.
        Check whether the draft review is consistent with the trip data and the policies:
        every violation must be real, no violation may be missed, and the verdict must follow
        from the violations. Accept only if the draft is correct. Otherwise give concrete feedback.
    """

    const val STRUCTURED = """
        Return the result as structured output
    """

    fun review(tripId: Long) = "Review trip with id $tripId and check if it is in compnay policies."

    fun revise(tripId: Long, draft: ReviewOutcome, judgement: Judgement) = revise(tripId, draft, judgement.feedback)

    fun revise(tripId: Long, draft: ReviewOutcome, feedback: String) = """
        Your previous review of trip $tripId was rejected by the auditor.
        Previous review: $draft
        Auditor feedback: $feedback
        Produce a corrected review.
    """.trimIndent()

    /** The facts the judge checks a draft against - one entry per piece of context. */
    fun judgeContext(trip: TripSnapshot, policies: List<PolicyText>, costs: CostBreakdown) = listOf(
        "Trip: $trip",
        "Costs: $costs",
        "Policies: ${policies.joinToString("\n") { "- ${it.title}: ${it.text}" }}",
    )

    fun judge(trip: TripSnapshot, policies: List<PolicyText>, costs: CostBreakdown, draft: ReviewOutcome) =
        judge(judgeContext(trip, policies, costs), draft.toString())

    fun judge(context: List<String>, draft: String) = (context + "Draft review: $draft").joinToString("\n")
}
