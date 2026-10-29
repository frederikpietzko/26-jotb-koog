package com.example.travel.review

import com.example.travel.fakes.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TravelFactsTest {

    private val world = InMemoryWorld()
    private val facts = world.facts

    @Test
    fun `adds up hotel nights, flights and trains`() {
        val id = world.service.create(aTripRequest()).id!!
        world.service.addHotel(id, aHotel(nightlyRate = "100.00", nights = 3))
        world.service.addFlight(id, aFlight(price = "200.00"))
        world.service.addTrain(id, aTrain(price = "50.00"))

        val costs = facts.costs(id)

        assertEquals(CostBreakdown(hotelNights = 3, hotels = 300.0, flights = 200.0, trains = 50.0, total = 550.0, currency = "EUR"), costs)
    }

    @Test
    fun `a trip without bookings costs nothing`() {
        val id = world.service.create(aTripRequest()).id!!

        assertEquals(0.0, facts.costs(id).total)
    }

    @Test
    fun `snapshot exposes travel durations in hours so the model does not have to calculate`() {
        val id = world.service.create(aTripRequest()).id!!
        world.service.addFlight(id, aFlight())
        world.service.addTrain(id, aTrain())

        val snapshot = facts.trip(id)

        assertEquals(1.5, snapshot.flights.single().durationHours)
        assertEquals(4.25, snapshot.trains.single().durationHours)
        assertEquals("Austrian OS111", snapshot.flights.single().flight)
    }

    @Test
    fun `lists every policy as plain text`() {
        val titles = facts.policies().map { it.title }

        assertEquals(listOf("Hotel budget", "Flight class"), titles)
    }
}
