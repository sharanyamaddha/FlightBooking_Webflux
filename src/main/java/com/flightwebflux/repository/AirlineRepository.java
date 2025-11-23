package com.flightwebflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightwebflux.model.Airline;

import reactor.core.publisher.Mono;

@Repository
public interface AirlineRepository extends ReactiveMongoRepository<Airline,String>{

	Mono<Airline> findByAirlineName(String airlineName);
}
