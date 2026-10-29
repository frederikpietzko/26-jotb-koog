package com.example.travel.trip

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

enum class TripStatus { DRAFT, SUBMITTED, IN_REVIEW, APPROVED, NEEDS_CHANGES, REJECTED, REVIEW_FAILED }

enum class CabinClass { ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST }

enum class TrainClass { SECOND, FIRST }

/**
 * The aggregate the agents review: one business trip with all of its bookings.
 */
@Entity
class Trip(
    var traveler: String,
    var purpose: String,
    var origin: String,
    var destination: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
    @Enumerated(EnumType.STRING)
    var status: TripStatus = TripStatus.DRAFT,
    var submittedAt: Instant? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id")
    val hotels: MutableSet<HotelBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id")
    val flights: MutableSet<FlightBooking> = linkedSetOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "trip_id")
    val trains: MutableSet<TrainBooking> = linkedSetOf(),
    @Id @GeneratedValue
    var id: Long? = null,
)

@Entity
class HotelBooking(
    var hotelName: String,
    var city: String,
    var checkIn: LocalDate,
    var checkOut: LocalDate,
    var nightlyRate: BigDecimal,
    var currency: String = "EUR",
    @Id @GeneratedValue
    var id: Long? = null,
)

@Entity
class FlightBooking(
    var airline: String,
    var flightNumber: String,
    var fromAirport: String,
    var toAirport: String,
    var departure: LocalDateTime,
    var arrival: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var cabinClass: CabinClass,
    var price: BigDecimal,
    var currency: String = "EUR",
    @Id @GeneratedValue
    var id: Long? = null,
)

@Entity
class TrainBooking(
    var operator: String,
    var fromStation: String,
    var toStation: String,
    var departure: LocalDateTime,
    var arrival: LocalDateTime,
    @Enumerated(EnumType.STRING)
    var travelClass: TrainClass,
    var price: BigDecimal,
    var currency: String = "EUR",
    @Id @GeneratedValue
    var id: Long? = null,
)
