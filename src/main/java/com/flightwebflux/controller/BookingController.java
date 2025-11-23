package com.flightwebflux.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.service.BookingService;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

public class BookingController {

	@Autowired
	BookingService bookingService;

}
