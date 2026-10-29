package com.example.travel.review

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

/** The persisted result of one review run. */
@Entity
class TripReview(
    val tripId: Long,
    @Enumerated(EnumType.STRING)
    val verdict: Verdict?,
    @Column(length = 4000)
    val summary: String,
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(length = 1000)
    val violations: List<String> = emptyList(),
    val durationMs: Long,
    val createdAt: Instant = Instant.now(),
    @Id @GeneratedValue
    var id: Long? = null,
)

interface TripReviewRepository : JpaRepository<TripReview, Long> {
    fun findByTripIdOrderByCreatedAtDesc(tripId: Long): List<TripReview>
}
