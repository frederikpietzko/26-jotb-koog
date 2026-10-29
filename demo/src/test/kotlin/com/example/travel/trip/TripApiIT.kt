package com.example.travel.trip

import com.example.travel.IntegrationTest
import com.jayway.jsonpath.JsonPath
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import kotlin.test.Test

@IntegrationTest
class TripApiIT(@Autowired private val mvc: MockMvc) {

    @Test
    fun `a trip unifies hotel, flight and train bookings`() {
        val id = createTrip()

        mvc.post("/api/trips/$id/hotels") { json(HOTEL) }.andExpect { status { isCreated() } }
        mvc.post("/api/trips/$id/flights") { json(FLIGHT) }.andExpect { status { isCreated() } }
        mvc.post("/api/trips/$id/trains") { json(TRAIN) }.andExpect { status { isCreated() } }

        mvc.get("/api/trips/$id").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("DRAFT") }
            jsonPath("$.hotels[0].hotelName") { value("Hotel Cubo") }
            jsonPath("$.flights[0].cabinClass") { value("ECONOMY") }
            jsonPath("$.trains[0].travelClass") { value("SECOND") }
        }
    }

    @Test
    fun `updates the trip itself`() {
        val id = createTrip()

        mvc.put("/api/trips/$id") { json(TRIP.replace("Ana Horvat", "Ivo Ivic")) }.andExpect {
            status { isOk() }
            jsonPath("$.traveler") { value("Ivo Ivic") }
        }
    }

    @Test
    fun `updates and removes a booking`() {
        val id = createTrip()
        val hotel = mvc.post("/api/trips/$id/hotels") { json(HOTEL) }.andReturn().response.contentAsString
        val hotelId = JsonPath.read<Number>(hotel, "$.hotels[0].id").toLong()

        mvc.put("/api/trips/$id/hotels/$hotelId") { json(HOTEL.replace("140.00", "99.00")) }.andExpect {
            jsonPath("$.hotels[0].nightlyRate") { value(99.0) }
        }
        mvc.delete("/api/trips/$id/hotels/$hotelId").andExpect {
            status { isOk() }
            jsonPath("$.hotels.length()") { value(0) }
        }
    }

    @Test
    fun `deletes a trip`() {
        val id = createTrip()

        mvc.delete("/api/trips/$id").andExpect { status { isNoContent() } }

        mvc.get("/api/trips/$id").andExpect { status { isNotFound() } }
    }

    @Test
    fun `unknown trips and bookings are 404`() {
        val id = createTrip()

        mvc.get("/api/trips/987654").andExpect { status { isNotFound() } }
        mvc.delete("/api/trips/$id/trains/987654").andExpect { status { isNotFound() } }
    }

    @Test
    fun `policies are plain text documents with full CRUD`() {
        val created = mvc.post("/api/policies") { json("""{"title":"Taxi","text":"Taxis only after 22:00."}""") }
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        val id = JsonPath.read<Number>(created, "$.id").toLong()

        mvc.put("/api/policies/$id") { json("""{"title":"Taxi","text":"Taxis only after 23:00."}""") }.andExpect {
            jsonPath("$.text") { value("Taxis only after 23:00.") }
        }
        mvc.get("/api/policies").andExpect { jsonPath("$[?(@.id == $id)].title") { value("Taxi") } }
        mvc.delete("/api/policies/$id").andExpect { status { isNoContent() } }
        mvc.get("/api/policies/$id").andExpect { status { isNotFound() } }
    }

    private fun createTrip(): Long {
        val body = mvc.post("/api/trips") { json(TRIP) }
            .andExpect { status { isCreated() } }
            .andReturn().response.contentAsString
        return JsonPath.read<Number>(body, "$.id").toLong()
    }

    private fun org.springframework.test.web.servlet.MockHttpServletRequestDsl.json(body: String) {
        contentType = MediaType.APPLICATION_JSON
        content = body
    }

    companion object {
        const val TRIP =
            """{"traveler":"Ana Horvat","purpose":"Workshop","origin":"Zagreb","destination":"Ljubljana","startDate":"2026-12-07","endDate":"2026-12-09"}"""
        const val HOTEL =
            """{"hotelName":"Hotel Cubo","city":"Ljubljana","checkIn":"2026-12-07","checkOut":"2026-12-09","nightlyRate":140.00}"""
        const val FLIGHT =
            """{"airline":"Croatia","flightNumber":"OU100","fromAirport":"ZAG","toAirport":"LJU","departure":"2026-12-07T07:00:00","arrival":"2026-12-07T07:45:00","cabinClass":"ECONOMY","price":120.00}"""
        const val TRAIN =
            """{"operator":"HZPP","fromStation":"Zagreb Glavni","toStation":"Ljubljana","departure":"2026-12-09T17:00:00","arrival":"2026-12-09T19:20:00","travelClass":"SECOND","price":19.00}"""
    }
}
