package com.flightwebflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightwebflux.model.Booking;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<Booking,String> {

}
