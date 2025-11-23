package com.flightwebflux.service;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.model.Booking;

import reactor.core.publisher.Mono;

public interface BookingService {

	Mono<Booking> createBooking(String flightId, BookingRequest request);
	
	
}
