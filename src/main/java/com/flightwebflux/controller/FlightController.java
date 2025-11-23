package com.flightwebflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.FlightResponse;
import com.flightwebflux.service.FlightService;
import java.util.List;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class FlightController {

	@Autowired
	FlightService flightService;
	
	@PostMapping("/flight/add")
	public Mono<ResponseEntity<String>> addFlights(@Valid @RequestBody FlightRequest request){
		return flightService.addFlights(request)
		.map(saved->ResponseEntity
				.status(HttpStatus.CREATED)
				.body(saved.getFlightId()));
	}
	
	@PostMapping("/flight/search")
	public Flux<FlightResponse> searchFlights(@RequestBody FlightRequest request){
		return flightService.searchFlights(request);
		
	}
	
}
