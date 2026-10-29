package com.example.travel.review

import com.example.travel.policy.PolicyRepository
import com.example.travel.trip.TripRepository
import com.example.travel.trip.notFound
import kotlinx.serialization.Serializable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Duration
import java.time.temporal.ChronoUnit

@Serializable
data class TripSnapshot(
    val id: Long,
    val traveler: String,
    val purpose: String,
    val origin: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val submittedAt: String?,
    val hotels: List<HotelLine>,
    val flights: List<FlightLine>,
    val trains: List<TrainLine>,
)

@Serializable
data class HotelLine(val hotel: String, val city: String, val checkIn: String, val checkOut: String, val nightlyRate: Double, val currency: String)

@Serializable
data class FlightLine(val flight: String, val from: String, val to: String, val departure: String, val durationHours: Double, val cabinClass: String, val price: Double, val currency: String)

@Serializable
data class TrainLine(val operator: String, val from: String, val to: String, val departure: String, val durationHours: Double, val travelClass: String, val price: Double, val currency: String)

@Serializable
data class PolicyText(val id: Long, val title: String, val text: String)

@Serializable
data class CostBreakdown(val hotelNights: Long, val hotels: Double, val flights: Double, val trains: Double, val total: Double, val currency: String)

/**
 * Facts the agent can look up - to be exposed as Koog tools.
 */
@Service
@Transactional(readOnly = true)
class TravelFacts(
    private val trips: TripRepository,
    private val policies: PolicyRepository,
) {

    fun trip(tripId: Long): TripSnapshot {
        val trip = trips.findByIdOrNull(tripId) ?: throw notFound("Trip $tripId")
        return TripSnapshot(
            id = tripId,
            traveler = trip.traveler,
            purpose = trip.purpose,
            origin = trip.origin,
            destination = trip.destination,
            startDate = trip.startDate.toString(),
            endDate = trip.endDate.toString(),
            submittedAt = trip.submittedAt?.toString(),
            hotels = trip.hotels.map {
                HotelLine(it.hotelName, it.city, it.checkIn.toString(), it.checkOut.toString(), it.nightlyRate.toDouble(), it.currency)
            },
            flights = trip.flights.map {
                FlightLine(
                    "${it.airline} ${it.flightNumber}", it.fromAirport, it.toAirport, it.departure.toString(),
                    hours(Duration.between(it.departure, it.arrival)), it.cabinClass.name, it.price.toDouble(), it.currency,
                )
            },
            trains = trip.trains.map {
                TrainLine(
                    it.operator, it.fromStation, it.toStation, it.departure.toString(),
                    hours(Duration.between(it.departure, it.arrival)), it.travelClass.name, it.price.toDouble(), it.currency,
                )
            },
        )
    }

    fun policies(): List<PolicyText> = policies.findAll().map { PolicyText(it.id!!, it.title, it.text) }

    fun costs(tripId: Long): CostBreakdown {
        val trip = trips.findByIdOrNull(tripId) ?: throw notFound("Trip $tripId")
        val nights = trip.hotels.sumOf { ChronoUnit.DAYS.between(it.checkIn, it.checkOut) }
        val hotels = trip.hotels.sumOf { it.nightlyRate * BigDecimal(ChronoUnit.DAYS.between(it.checkIn, it.checkOut)) }
        val flights = trip.flights.sumOf { it.price }
        val trains = trip.trains.sumOf { it.price }
        return CostBreakdown(nights, hotels.toDouble(), flights.toDouble(), trains.toDouble(), (hotels + flights + trains).toDouble(), "EUR")
    }

    private fun hours(duration: Duration) = duration.toMinutes() / 60.0
}
