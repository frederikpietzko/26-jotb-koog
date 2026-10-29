package com.example.travel.fakes

import com.example.travel.policy.Policy
import com.example.travel.review.TravelFacts
import com.example.travel.trip.*
import java.math.BigDecimal
import java.time.LocalDate

val START: LocalDate = LocalDate.of(2026, 11, 2)

fun aTripRequest(traveler: String = "Marko Kovac", destination: String = "Munich") =
    TripRequest(traveler, "Conference", "Vienna", destination, START, START.plusDays(3))

fun aHotel(nightlyRate: String = "520.00", nights: Long = 3) =
    HotelRequest("Mandarin Oriental", "Munich", START, START.plusDays(nights), BigDecimal(nightlyRate))

fun aFlight(cabinClass: CabinClass = CabinClass.BUSINESS, price: String = "640.00") =
    FlightRequest("Austrian", "OS111", "VIE", "MUC", START.atTime(8, 0), START.atTime(9, 30), cabinClass, BigDecimal(price))

fun aTrain(price: String = "39.00") =
    TrainRequest("OBB", "Wien Hbf", "Muenchen Hbf", START.atTime(7, 0), START.atTime(11, 15), TrainClass.SECOND, BigDecimal(price))

/** A fully wired, in-memory world: repositories, service and facts, seeded with one violating trip. */
class InMemoryWorld {
    val trips = InMemoryTripRepository()
    val policies = InMemoryPolicyRepository()
    val events = RecordingEventPublisher()
    val service = TripService(trips, events)
    val facts = TravelFacts(trips, policies)

    init {
        policies.save(Policy("Hotel budget", "Hotels may cost at most 180 EUR per night."))
        policies.save(Policy("Flight class", "Economy class is mandatory for flights shorter than 6 hours."))
    }

    fun violatingTrip(): Long = service.create(aTripRequest()).id!!.also {
        service.addHotel(it, aHotel())
        service.addFlight(it, aFlight())
    }
}
