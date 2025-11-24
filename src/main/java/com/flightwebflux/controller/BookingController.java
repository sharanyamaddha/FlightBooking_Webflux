package com.flightwebflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.repository.AirlineRepository;
import com.flightwebflux.service.BookingService;

import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class BookingController {

    

	@Autowired
	BookingService bookingService;

	
	@PostMapping("/booking/{flightId}")
	public Mono<ResponseEntity<String>> createBooking(@PathVariable("flightId") String flightId,@Valid @RequestBody BookingRequest request){
		return bookingService.createBooking(flightId,request)
				.map(saved->ResponseEntity
						.status(HttpStatus.CREATED)
						.body(saved.getPnr()));
			
	}
	
	@GetMapping("/booking/{pnr}")
	public Mono<ResponseEntity<BookingResponse>> getBookingByPnr(@PathVariable("pnr") String pnr){
		return bookingService.getBookingByPnr(pnr)
				.map(saved->ResponseEntity.ok(saved));
		
	}
	
	@GetMapping("/booking/history/{email}")
	public Flux<BookingResponse> getBookingHistory(@PathVariable("email") String email){
		return bookingService.getBookingHistory(email);
		
	}
	
	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<ResponseEntity<String>> cancelBooking(@PathVariable("pnr") String pnr){
		return bookingService.cancelBooking(pnr)
				.map(message -> ResponseEntity.ok(message));
		
	}


}
