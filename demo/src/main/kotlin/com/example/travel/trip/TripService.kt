package com.example.travel.trip

import com.example.travel.review.TripSubmitted
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

data class TripRequest(
    val traveler: String,
    val purpose: String,
    val origin: String,
    val destination: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)

data class HotelRequest(
    val hotelName: String,
    val city: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val nightlyRate: BigDecimal,
    val currency: String = "EUR",
)

data class FlightRequest(
    val airline: String,
    val flightNumber: String,
    val fromAirport: String,
    val toAirport: String,
    val departure: LocalDateTime,
    val arrival: LocalDateTime,
    val cabinClass: CabinClass,
    val price: BigDecimal,
    val currency: String = "EUR",
)

data class TrainRequest(
    val operator: String,
    val fromStation: String,
    val toStation: String,
    val departure: LocalDateTime,
    val arrival: LocalDateTime,
    val travelClass: TrainClass,
    val price: BigDecimal,
    val currency: String = "EUR",
)

@Service
@Transactional
class TripService(
    private val trips: TripRepository,
    private val events: ApplicationEventPublisher,
) {

    @Transactional(readOnly = true)
    fun all(): List<Trip> = trips.findAll()

    @Transactional(readOnly = true)
    fun get(id: Long): Trip = trips.findByIdOrNull(id) ?: throw notFound("Trip $id")

    fun create(request: TripRequest): Trip = trips.save(
        Trip(
            traveler = request.traveler,
            purpose = request.purpose,
            origin = request.origin,
            destination = request.destination,
            startDate = request.startDate,
            endDate = request.endDate,
        )
    )

    fun update(id: Long, request: TripRequest): Trip = editable(id).apply {
        traveler = request.traveler
        purpose = request.purpose
        origin = request.origin
        destination = request.destination
        startDate = request.startDate
        endDate = request.endDate
    }

    fun delete(id: Long) = trips.delete(get(id))

    fun addHotel(tripId: Long, r: HotelRequest): Trip = editable(tripId).apply {
        hotels += HotelBooking(r.hotelName, r.city, r.checkIn, r.checkOut, r.nightlyRate, r.currency)
    }

    fun updateHotel(tripId: Long, hotelId: Long, r: HotelRequest): Trip = editable(tripId).apply {
        hotels.find(hotelId).apply {
            hotelName = r.hotelName; city = r.city; checkIn = r.checkIn; checkOut = r.checkOut
            nightlyRate = r.nightlyRate; currency = r.currency
        }
    }

    fun removeHotel(tripId: Long, hotelId: Long): Trip = editable(tripId).apply {
        hotels.remove(hotels.find(hotelId))
    }

    fun addFlight(tripId: Long, r: FlightRequest): Trip = editable(tripId).apply {
        flights += FlightBooking(
            r.airline, r.flightNumber, r.fromAirport, r.toAirport,
            r.departure, r.arrival, r.cabinClass, r.price, r.currency,
        )
    }

    fun updateFlight(tripId: Long, flightId: Long, r: FlightRequest): Trip = editable(tripId).apply {
        flights.find(flightId).apply {
            airline = r.airline; flightNumber = r.flightNumber; fromAirport = r.fromAirport; toAirport = r.toAirport
            departure = r.departure; arrival = r.arrival; cabinClass = r.cabinClass; price = r.price; currency = r.currency
        }
    }

    fun removeFlight(tripId: Long, flightId: Long): Trip = editable(tripId).apply {
        flights.remove(flights.find(flightId))
    }

    fun addTrain(tripId: Long, r: TrainRequest): Trip = editable(tripId).apply {
        trains += TrainBooking(
            r.operator, r.fromStation, r.toStation, r.departure, r.arrival, r.travelClass, r.price, r.currency,
        )
    }

    fun updateTrain(tripId: Long, trainId: Long, r: TrainRequest): Trip = editable(tripId).apply {
        trains.find(trainId).apply {
            operator = r.operator; fromStation = r.fromStation; toStation = r.toStation
            departure = r.departure; arrival = r.arrival; travelClass = r.travelClass; price = r.price; currency = r.currency
        }
    }

    fun removeTrain(tripId: Long, trainId: Long): Trip = editable(tripId).apply {
        trains.remove(trains.find(trainId))
    }

    /**
     * The hand-over point: the trip is frozen and a review job is queued.
     * The actual review runs asynchronously once this transaction commits.
     */
    fun submit(id: Long): Trip {
        val trip = editable(id)
        trip.status = TripStatus.SUBMITTED
        trip.submittedAt = Instant.now()
        events.publishEvent(TripSubmitted(id))
        return trip
    }

    fun updateStatus(id: Long, status: TripStatus) {
        get(id).status = status
    }

    private fun editable(id: Long): Trip = get(id).also {
        if (it.status !in setOf(TripStatus.DRAFT, TripStatus.NEEDS_CHANGES, TripStatus.REVIEW_FAILED)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Trip $id is ${it.status} and cannot be changed")
        }
    }

    private fun Set<HotelBooking>.find(id: Long) = firstOrNull { it.id == id } ?: throw notFound("Hotel booking $id")

    @JvmName("findFlight")
    private fun Set<FlightBooking>.find(id: Long) = firstOrNull { it.id == id } ?: throw notFound("Flight booking $id")

    @JvmName("findTrain")
    private fun Set<TrainBooking>.find(id: Long) = firstOrNull { it.id == id } ?: throw notFound("Train booking $id")
}

fun notFound(what: String) = ResponseStatusException(HttpStatus.NOT_FOUND, "$what not found")
