package com.flightwebflux.service;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.model.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {

	Mono<Booking> createBooking(String flightId, BookingRequest request);
	
	Mono<BookingResponse> getBookingByPnr(String pnr);
	
	Flux<BookingResponse> getBookingHistory(String email);
}
