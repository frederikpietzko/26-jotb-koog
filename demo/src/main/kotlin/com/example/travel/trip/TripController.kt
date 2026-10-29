package com.example.travel.trip

import com.example.travel.review.TripReview
import com.example.travel.review.TripReviewRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/trips")
class TripController(
    private val service: TripService,
    private val reviews: TripReviewRepository,
) {

    @GetMapping
    fun all(): List<Trip> = service.all()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): Trip = service.get(id)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: TripRequest): Trip = service.create(request)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody request: TripRequest): Trip = service.update(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)

    /** Submits the trip and queues a review by the agent. */
    @PostMapping("/{id}/submit")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun submit(@PathVariable id: Long): Trip = service.submit(id)

    @GetMapping("/{id}/reviews")
    fun reviews(@PathVariable id: Long): List<TripReview> = reviews.findByTripIdOrderByCreatedAtDesc(id)

    // --- hotels ---

    @PostMapping("/{id}/hotels")
    @ResponseStatus(HttpStatus.CREATED)
    fun addHotel(@PathVariable id: Long, @RequestBody r: HotelRequest) = service.addHotel(id, r)

    @PutMapping("/{id}/hotels/{bookingId}")
    fun updateHotel(@PathVariable id: Long, @PathVariable bookingId: Long, @RequestBody r: HotelRequest) =
        service.updateHotel(id, bookingId, r)

    @DeleteMapping("/{id}/hotels/{bookingId}")
    fun removeHotel(@PathVariable id: Long, @PathVariable bookingId: Long) = service.removeHotel(id, bookingId)

    // --- flights ---

    @PostMapping("/{id}/flights")
    @ResponseStatus(HttpStatus.CREATED)
    fun addFlight(@PathVariable id: Long, @RequestBody r: FlightRequest) = service.addFlight(id, r)

    @PutMapping("/{id}/flights/{bookingId}")
    fun updateFlight(@PathVariable id: Long, @PathVariable bookingId: Long, @RequestBody r: FlightRequest) =
        service.updateFlight(id, bookingId, r)

    @DeleteMapping("/{id}/flights/{bookingId}")
    fun removeFlight(@PathVariable id: Long, @PathVariable bookingId: Long) = service.removeFlight(id, bookingId)

    // --- trains ---

    @PostMapping("/{id}/trains")
    @ResponseStatus(HttpStatus.CREATED)
    fun addTrain(@PathVariable id: Long, @RequestBody r: TrainRequest) = service.addTrain(id, r)

    @PutMapping("/{id}/trains/{bookingId}")
    fun updateTrain(@PathVariable id: Long, @PathVariable bookingId: Long, @RequestBody r: TrainRequest) =
        service.updateTrain(id, bookingId, r)

    @DeleteMapping("/{id}/trains/{bookingId}")
    fun removeTrain(@PathVariable id: Long, @PathVariable bookingId: Long) = service.removeTrain(id, bookingId)
}
