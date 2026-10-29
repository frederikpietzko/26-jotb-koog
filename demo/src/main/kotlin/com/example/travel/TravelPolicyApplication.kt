package com.example.travel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class TravelPolicyApplication

fun main(args: Array<String>) {
    runApplication<TravelPolicyApplication>(*args)
}
