package com.flightwebflux.serviceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.enums.BookingStatus;
import com.flightwebflux.enums.TripType;
import com.flightwebflux.exceptions.BusinessException;
import com.flightwebflux.model.Booking;
import com.flightwebflux.model.Flight;
import com.flightwebflux.model.Passenger;
import com.flightwebflux.repository.BookingRepository;
import com.flightwebflux.repository.FlightRepository;
import com.flightwebflux.repository.PassengerRepository;
import com.flightwebflux.service.BookingService;

import reactor.core.publisher.Mono;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	FlightRepository flightRepository;

	@Autowired
	BookingRepository bookingRepository;

	@Autowired
	PassengerRepository passengerRepository;

	@Override
	public Mono<Booking> createBooking(String flightId,BookingRequest request){
		
		return flightRepository.findById(flightId)
				.switchIfEmpty(Mono.error(new BusinessException("Flight not found with id: "+flightId)))
				.flatMap(flight->{
					
	
					int passengerCount=request.getPassengers().size();
					
					if (flight.getAvailableSeats() < passengerCount) {
	                    return Mono.error(new BusinessException("Not enough seats available"));
					}
					
				 String pnr = "PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
				Booking booking=new Booking();
				booking.setPnr(pnr);
				booking.setFlightId(flight.getFlightId());
				booking.setBookerEmailId(request.getBookerEmailId());
				booking.setStatus(BookingStatus.BOOKED);
				booking.setBookingDateTime(LocalDateTime.now());
				
				
				double price=flight.getPrice();
				double totalAmount=price*passengerCount;
				booking.setTotalAmount(totalAmount);
								
				return bookingRepository.save(booking)
					.flatMap(savedBooking->{
					
						flight.setAvailableSeats(flight.getAvailableSeats()-passengerCount);
						Mono<Flight> updatedFlightMono=flightRepository.save(flight);
				
						Mono<Void> savePassengersMono = passengerRepository
	                                    .saveAll(
	                    request.getPassengers().stream()
	                            .map(pReq -> {
	                                Passenger p = new Passenger();
	                                p.setName(pReq.getName());
	                                p.setAge(pReq.getAge());
	                                p.setGender(pReq.getGender());
	                                p.setSeatNo(pReq.getSeatNo());
	                                p.setMealType(pReq.getMealType());
	                                p.setFlightId(flight.getFlightId());
	                                p.setPnr(savedBooking.getPnr());
	                                return p;
								}).toList()
	                            )
	                             .then();	
						return Mono.when(updatedFlightMono, savePassengersMono)
                                .thenReturn(savedBooking);
						        
							});
				});	
	}
	
}
