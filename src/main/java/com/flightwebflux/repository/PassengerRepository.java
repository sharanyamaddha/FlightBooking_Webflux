package com.flightwebflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightwebflux.model.Passenger;

@Repository
public interface PassengerRepository extends ReactiveMongoRepository<Passenger,String>{

}
