package com.example.travel

import com.example.travel.policy.Policy
import com.example.travel.policy.PolicyRepository
import com.example.travel.trip.*
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.LocalDate

/** Seeds policies and two trips: one compliant, one that breaks several rules. */
@Configuration
class DemoData {

    @Bean
    fun seed(policies: PolicyRepository, trips: TripService) = ApplicationRunner {
        policies.saveAll(
            listOf(
                Policy("Hotel budget", "Hotels may cost at most 180 EUR per night. In London, Paris and Zurich the limit is 250 EUR per night."),
                Policy("Flight class", "Economy class is mandatory for flights shorter than 6 hours. Business class is allowed for longer flights."),
                Policy("Rail first", "If the train journey takes 4 hours or less, the train must be used instead of a flight. Second class only."),
                Policy("Booking lead time", "Trips must be submitted at least 14 days before the start date."),
                Policy("Weekend stays", "Hotel nights on Friday and Saturday are only reimbursed if a business meeting takes place on the following day."),
            )
        )

        val inTwoMonths = LocalDate.now().plusMonths(2).with(java.time.DayOfWeek.MONDAY)

        // compliant: train Zagreb -> Ljubljana, modest hotel
        trips.create(TripRequest("Ana Horvat", "Customer workshop", "Zagreb", "Ljubljana", inTwoMonths, inTwoMonths.plusDays(2))).also {
            trips.addTrain(it.id!!, TrainRequest("HZPP", "Zagreb Glavni", "Ljubljana", inTwoMonths.atTime(7, 0), inTwoMonths.atTime(9, 20), TrainClass.SECOND, BigDecimal("19.00")))
            trips.addHotel(it.id!!, HotelRequest("Hotel Cubo", "Ljubljana", inTwoMonths, inTwoMonths.plusDays(2), BigDecimal("140.00")))
            trips.addTrain(it.id!!, TrainRequest("HZPP", "Ljubljana", "Zagreb Glavni", inTwoMonths.plusDays(2).atTime(17, 0), inTwoMonths.plusDays(2).atTime(19, 20), TrainClass.SECOND, BigDecimal("19.00")))
        }

        // violating: business class on a short flight, pricey hotel, short notice
        val soon = LocalDate.now().plusDays(5)
        trips.create(TripRequest("Marko Kovac", "Conference J On The Beach", "Vienna", "Munich", soon, soon.plusDays(3))).also {
            trips.addFlight(it.id!!, FlightRequest("Austrian", "OS111", "VIE", "MUC", soon.atTime(8, 0), soon.atTime(9, 5), CabinClass.BUSINESS, BigDecimal("640.00")))
            trips.addHotel(it.id!!, HotelRequest("Mandarin Oriental", "Munich", soon, soon.plusDays(3), BigDecimal("520.00")))
        }
    }
}
