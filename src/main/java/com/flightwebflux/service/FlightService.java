package com.flightwebflux.service;

import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.FlightResponse;
import com.flightwebflux.model.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {

	Mono<Flight> addFlights(FlightRequest request);
	
	Flux<FlightResponse> searchFlights(FlightRequest request);
}
