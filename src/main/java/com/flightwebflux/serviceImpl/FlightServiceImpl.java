package com.flightwebflux.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.FlightResponse;
import com.flightwebflux.exceptions.BusinessException;
import com.flightwebflux.model.Airline;
import com.flightwebflux.model.Flight;
import com.flightwebflux.repository.AirlineRepository;
import com.flightwebflux.repository.FlightRepository;
import com.flightwebflux.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightServiceImpl implements FlightService {

	@Autowired
	FlightRepository flightRepository;
	
	@Autowired
	AirlineRepository airlineRepository;

	@Override
	public Mono<Flight> addFlights(FlightRequest request) {

	    if (request.getArrivalDateTime().isBefore(request.getDepartureDateTime())) {
	        return Mono.error(new BusinessException("Arrival time must be after departure time"));
	    }

	    Mono<Airline> airlineMono = airlineRepository.findByAirlineName(request.getAirlineName())
	            .switchIfEmpty(Mono.defer(() -> {
	                Airline a = new Airline();
	                a.setAirlineName(request.getAirlineName());
	                return airlineRepository.save(a);
	            }));

	    return airlineMono.flatMap(airline ->
	            flightRepository.findByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
	                    airline.getAirlineId(),
	                    request.getSource(),
	                    request.getDestination(),
	                    request.getDepartureDateTime()
	            )
	            .hasElement()
	            .flatMap(exists -> {

	                if (exists) {
	                    return Mono.error(new BusinessException("Flight already exists for this airline at this time"));
	                }

	                Flight flight = new Flight();
	                flight.setAirlineId(airline.getAirlineId());
	                flight.setSource(request.getSource());
	                flight.setDestination(request.getDestination());
	                flight.setDepartureDateTime(request.getDepartureDateTime());
	                flight.setArrivalDateTime(request.getArrivalDateTime());
	                flight.setTotalSeats(request.getTotalSeats());
	                flight.setAvailableSeats(request.getTotalSeats());
	                flight.setPrice(request.getPrice());
	                
	                String airlineCode = airline.getAirlineName()
                            .substring(0, Math.min(2, airline.getAirlineName().length()))
                            .toUpperCase();

                    int randomNumber = (int) (Math.random() * 900) + 100; 

                    String flightNumber = airlineCode + "-" + randomNumber;
                    flight.setFlightNo(flightNumber);
                    
	                return flightRepository.save(flight);

	            })
	    );
	}
	
	
	@Override
	public Flux<FlightResponse> searchFlights(FlightRequest request){
		String source=request.getSource();
		String destination=request.getDestination();
		String airlineName=request.getAirlineName();
		

		
		if (airlineName == null) {
	        return flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase(source, destination)
	        		.flatMap(this::mapFlightToResponse);
	    } 
		
		//searching by airlineName
		return airlineRepository.findByAirlineName(airlineName)
				.switchIfEmpty(Mono.error(new BusinessException("Airline not found")))
				.flatMapMany(airline ->
                flightRepository
                        .findByAirlineIdIgnoreCase(
                                airline.getAirlineId()
                        )
                        .flatMap(this::mapFlightToResponse)
        );
				
	}

		
	private Mono<FlightResponse> mapFlightToResponse(Flight flight) {
	    return airlineRepository.findById(flight.getAirlineId())
	            .map(airline -> {
	                FlightResponse res = new FlightResponse();
	                res.setFlightNo(flight.getFlightNo());
	                res.setAirlineName(airline.getAirlineName());
	                res.setSource(flight.getSource());
	                res.setDestination(flight.getDestination());
	                res.setDepartureDateTime(flight.getDepartureDateTime());
	                res.setArrivalDateTime(flight.getArrivalDateTime());
	                res.setAvailableSeats(flight.getAvailableSeats());
	                res.setPrice(flight.getPrice());
	                return res;
	            });
		
		
		
	}
	
}

