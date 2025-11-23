package com.flightwebflux.repository;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightwebflux.model.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FlightRepository extends ReactiveMongoRepository<Flight,String>{
	
	Mono<Flight> findByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
            String airlineId,
            String source,
            String destination,
            LocalDateTime departureDateTime
    );
	
	Flux<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source,String destination);
	
	Flux<Flight> findByAirlineIdIgnoreCase(String airlineId);
	
	
//	Flux<Flight> findByAirlineIdAndSourceIgnoreCaseAndDestinationIgnoreCase(
//            String airlineId,
//            String source,
//            String destination
//    );
}
