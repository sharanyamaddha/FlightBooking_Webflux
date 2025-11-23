package com.flightwebflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.service.BookingService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
public class BookingController {

	@Autowired
	BookingService bookingService;

	
	@PostMapping("/flight/booking/{flightId}")
	public Mono<ResponseEntity<String>> createBooking(@PathVariable("flightId") String flightId,@Valid @RequestBody BookingRequest request){
		return bookingService.createBooking(flightId,request)
				.map(saved->ResponseEntity
						.status(HttpStatus.CREATED)
						.body(saved.getPnr()));
			
	}
	
	@GetMapping("/flight/booking/{pnr}")
	public Mono<ResponseEntity<BookingResponse>> getBookingByPnr(@PathVariable("pnr") String pnr){
		return bookingService.getBookingByPnr(pnr)
				.map(saved->ResponseEntity.ok(saved));
		
	}


}
