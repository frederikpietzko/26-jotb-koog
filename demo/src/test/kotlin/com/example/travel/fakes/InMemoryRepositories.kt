package com.example.travel.fakes

import com.example.travel.policy.Policy
import com.example.travel.policy.PolicyRepository
import com.example.travel.trip.Trip
import com.example.travel.trip.TripRepository
import org.springframework.context.ApplicationEventPublisher
import java.lang.reflect.Proxy
import java.util.Optional
import java.util.concurrent.atomic.AtomicLong

/** Fails loudly for every repository method a fake does not implement. */
private inline fun <reified T : Any> unsupported(): T = Proxy.newProxyInstance(
    T::class.java.classLoader, arrayOf(T::class.java),
) { _, method, _ -> throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not faked") } as T

private val ids = AtomicLong()

/**
 * Mimics JPA for the aggregate: ids are generated on save, and bookings added to a managed trip
 * get their ids on the next read - just like dirty checking flushes them at the end of a transaction.
 */
class InMemoryTripRepository : TripRepository by unsupported() {
    private val rows = linkedMapOf<Long, Trip>()

    override fun <S : Trip> save(entity: S): S = entity.also { trip ->
        trip.id = trip.id ?: ids.incrementAndGet()
        rows[trip.id!!] = trip
        assignBookingIds()
    }

    override fun findById(id: Long): Optional<Trip> = Optional.ofNullable(rows[id]).also { assignBookingIds() }
    override fun findAll(): List<Trip> = rows.values.toList().also { assignBookingIds() }

    private fun assignBookingIds() = rows.values.forEach { trip ->
        trip.hotels.forEach { it.id = it.id ?: ids.incrementAndGet() }
        trip.flights.forEach { it.id = it.id ?: ids.incrementAndGet() }
        trip.trains.forEach { it.id = it.id ?: ids.incrementAndGet() }
    }
    override fun delete(entity: Trip) {
        rows.remove(entity.id)
    }
}

class InMemoryPolicyRepository : PolicyRepository by unsupported() {
    private val rows = linkedMapOf<Long, Policy>()

    override fun <S : Policy> save(entity: S): S = entity.also {
        it.id = it.id ?: ids.incrementAndGet()
        rows[it.id!!] = it
    }

    override fun findById(id: Long): Optional<Policy> = Optional.ofNullable(rows[id])
    override fun findAll(): List<Policy> = rows.values.toList()
}

class RecordingEventPublisher : ApplicationEventPublisher {
    private val events = mutableListOf<Any>()
    val published: List<Any> get() = events

    override fun publishEvent(event: Any) {
        events += event
    }
}
