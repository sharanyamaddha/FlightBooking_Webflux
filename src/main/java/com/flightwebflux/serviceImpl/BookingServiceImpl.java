package com.flightwebflux.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.model.Booking;
import com.flightwebflux.repository.BookingRepository;
import com.flightwebflux.repository.FlightRepository;
import com.flightwebflux.repository.PassengerRepository;
import com.flightwebflux.service.BookingService;

import reactor.core.publisher.Mono;

@Service
public class BookingServiceImpl implements BookingService{

	@Autowired
	FlightRepository flightRepository;
	
	@Autowired
	BookingRepository bookingRepository;
	
	@Autowired
	PassengerRepository passengerRepository;
	
	@Override
	public Mono<Booking> createBooking(String flightId,BookingRequest request){
		
		Flight flight=flightRepository.findById(flightId)
				
		
	}
	
	
}
