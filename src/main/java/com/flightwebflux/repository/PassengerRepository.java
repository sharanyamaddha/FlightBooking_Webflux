package com.flightwebflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightwebflux.model.Passenger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PassengerRepository extends ReactiveMongoRepository<Passenger,String>{

	Flux<Passenger> findByPnr(String pnr);

	 Mono<Long> countByPnr(String pnr); 
}
