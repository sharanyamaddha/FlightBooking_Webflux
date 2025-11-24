package com.flightwebflux.serviceImpl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.request.PassengerRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.dto.response.PassengerResponse;
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

import reactor.core.publisher.Flux;
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
					
					List<String> seatNos = request.getPassengers().stream()
						    .map(PassengerRequest::getSeatNo)
						    .filter(Objects::nonNull)
						    .map(s -> s.trim().toUpperCase())
						    .toList();

						return passengerRepository.findByFlightIdAndSeatNoIn(flightId, seatNos)
						    .collectList()
						    .flatMap(conflicts -> {
						        if (!conflicts.isEmpty()) {
						            String taken = conflicts.stream()
						                .map(Passenger::getSeatNo)
						                .distinct()
						                .collect(Collectors.joining(", "));
						            return Mono.error(new BusinessException("Seat(s) already taken: " + taken));
						        }
							
				
					
				String pnr = "PNR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
				Booking booking=new Booking();
				booking.setPnr(pnr);
				booking.setFlightId(flight.getFlightId());
				booking.setBookerEmailId(request.getBookerEmailId());
				booking.setStatus(BookingStatus.BOOKED);
				booking.setTripType(request.getTripType());
				booking.setBookingDateTime(LocalDateTime.now());
				booking.setSeatsBooked(passengerCount);
				
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
				
			});
	}
	
	
	
	@Override
	public Mono<BookingResponse> getBookingByPnr(String pnr){
		return bookingRepository.findByPnr(pnr)
				.switchIfEmpty(Mono.error(new BusinessException("invalid PNR")))
				.flatMap(this::mapToBookingResponse);
	}
	
	private Mono<BookingResponse> mapToBookingResponse(Booking booking) {
		Mono<List<Passenger>> passengersMono =
	            passengerRepository.findByPnr(booking.getPnr()).collectList();

	    Mono<Flight> flightMono =
	            flightRepository.findById(booking.getFlightId());
		
	    return Mono.zip(flightMono, passengersMono)
	            .map(tuple -> {
	                Flight flight = tuple.getT1();
	                List<Passenger> passengers = tuple.getT2();
	                
					BookingResponse res=new BookingResponse(); 
			        res.setPnr(booking.getPnr());
			        res.setStatus(booking.getStatus());
			        res.setTripType(booking.getTripType());
			        res.setTotalAmount(booking.getTotalAmount());
			        res.setBookingDateTime(booking.getBookingDateTime());
			        res.setBookerEmailId(booking.getBookerEmailId());
			        res.setSource(flight.getSource());
			        res.setDestination(flight.getDestination());
			        res.setAirlineName(flight.getAirlineName());
			       // res.setFlightId(booking.getFlightId());
			        
			        List<PassengerResponse> passengerResponses=passengers.stream()
			        	.map(p->{
			        		PassengerResponse pr=new PassengerResponse();
			        		pr.setName(p.getName());
			        		pr.setAge(p.getAge());
			                pr.setGender(p.getGender());
			                pr.setSeatNo(p.getSeatNo());
			                pr.setMealType(p.getMealType());
			                return pr;	
			        	})
			        	.toList();
			        res.setPassengers(passengerResponses);
			        return res;
				});
	}
	
	
	@Override
	public Flux<BookingResponse> getBookingHistory(String bookerEmailId){
		return bookingRepository.findByBookerEmailIdOrderByBookingDateTimeDesc(bookerEmailId)
				 .flatMap(this::mapToBookingResponse);
		    	    

				}
		
	
	
	@Override
	public Mono<String> cancelBooking(String pnr){
		return bookingRepository.findByPnr(pnr)
				.switchIfEmpty(Mono.error(new BusinessException("Invalid PNR")))
	            .flatMap(booking -> {
	            	if(booking.getStatus()==BookingStatus.CANCELLED) {
	            		return Mono.error(new BusinessException("Booking already cancelled"));
	            	}
	            	
	            	 return flightRepository.findById(booking.getFlightId())
	                         .switchIfEmpty(Mono.error(new BusinessException("Flight not found for this booking")))
	                         .flatMap(flight -> {
	                        	 LocalDateTime bookingTime = booking.getBookingDateTime();
	                        	 LocalDateTime now = LocalDateTime.now();

	                        	 if (Duration.between(bookingTime, now).toHours() >= 24) {
	                        	     return Mono.error(new BusinessException("Cancellation allowed only within 24 hours of booking"));
	                        	 }

	                             booking.setStatus(BookingStatus.CANCELLED);
	                             Mono<Booking> updatedBookingMono = bookingRepository.save(booking);
	                             
	                             Mono<Long> passengerCountMono = passengerRepository.countByPnr(pnr);

	                             Mono<Flight> updatedFlightMono = passengerCountMono
	                                     .flatMap(count -> {
	                                         int seatsToFree = count.intValue();
	                                         flight.setAvailableSeats(flight.getAvailableSeats() + seatsToFree);
	                                         return flightRepository.save(flight);
	                                     });
	                             
	                             return Mono.when(updatedBookingMono, updatedFlightMono)
	                                     .thenReturn("Booking cancelled successfully");
	                         });
	            });

		
	}
	
}
