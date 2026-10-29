package com.example.travel.trip

import com.example.travel.fakes.*
import com.example.travel.review.TripSubmitted
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TripServiceTest {

    private val world = InMemoryWorld()
    private val service = world.service

    @Test
    fun `creates a trip as draft`() {
        val trip = service.create(aTripRequest())

        assertNotNull(trip.id)
        assertEquals(TripStatus.DRAFT, service.get(trip.id!!).status)
    }

    @Test
    fun `unknown trip is reported as not found`() {
        val error = assertFailsWith<ResponseStatusException> { service.get(4711) }

        assertEquals(HttpStatus.NOT_FOUND, error.statusCode)
    }

    @Test
    fun `bookings of all three kinds end up on the same trip`() {
        val id = service.create(aTripRequest()).id!!

        service.addHotel(id, aHotel())
        service.addFlight(id, aFlight())
        val trip = service.addTrain(id, aTrain())

        assertEquals(1, trip.hotels.size)
        assertEquals(1, trip.flights.size)
        assertEquals(1, trip.trains.size)
    }

    @Test
    fun `updates and removes a booking by its id`() {
        val id = service.create(aTripRequest()).id!!
        service.addHotel(id, aHotel())
        val hotelId = service.get(id).hotels.single().id!!

        val updated = service.updateHotel(id, hotelId, aHotel(nightlyRate = "150.00"))
        assertEquals(BigDecimal("150.00"), updated.hotels.single().nightlyRate)

        val removed = service.removeHotel(id, hotelId)
        assertTrue(removed.hotels.isEmpty())
    }

    @Test
    fun `unknown booking is reported as not found`() {
        val id = service.create(aTripRequest()).id!!

        val error = assertFailsWith<ResponseStatusException> { service.removeFlight(id, 4711) }

        assertEquals(HttpStatus.NOT_FOUND, error.statusCode)
    }

    @Nested
    inner class Submitting {

        @Test
        fun `freezes the trip and queues a review`() {
            val id = world.violatingTrip()

            val trip = service.submit(id)

            assertEquals(TripStatus.SUBMITTED, trip.status)
            assertNotNull(trip.submittedAt)
            assertEquals(listOf<Any>(TripSubmitted(id)), world.events.published)
        }

        @Test
        fun `a submitted trip can no longer be changed`() {
            val id = world.violatingTrip()
            service.submit(id)

            val error = assertFailsWith<ResponseStatusException> { service.addTrain(id, aTrain()) }

            assertEquals(HttpStatus.CONFLICT, error.statusCode)
        }

        @Test
        fun `a submitted trip cannot be submitted twice`() {
            val id = world.violatingTrip()
            service.submit(id)

            assertFailsWith<ResponseStatusException> { service.submit(id) }

            assertEquals(1, world.events.published.size)
        }

        @ParameterizedTest
        @EnumSource(TripStatus::class, names = ["DRAFT", "NEEDS_CHANGES", "REVIEW_FAILED"])
        fun `trips that are open for changes can be resubmitted`(status: TripStatus) {
            val id = world.violatingTrip()
            service.updateStatus(id, status)

            val trip = service.submit(id)

            assertEquals(TripStatus.SUBMITTED, trip.status)
        }

        @ParameterizedTest
        @EnumSource(TripStatus::class, names = ["SUBMITTED", "IN_REVIEW", "APPROVED", "REJECTED"])
        fun `trips in review or decided are locked`(status: TripStatus) {
            val id = world.violatingTrip()
            service.updateStatus(id, status)

            val error = assertFailsWith<ResponseStatusException> { service.update(id, aTripRequest(traveler = "Ana")) }

            assertEquals(HttpStatus.CONFLICT, error.statusCode)
        }
    }
}
