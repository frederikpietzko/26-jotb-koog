package com.example.travel.trip

import org.springframework.data.jpa.repository.JpaRepository

interface TripRepository : JpaRepository<Trip, Long>
