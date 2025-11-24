package com.flightwebflux;



import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.exceptions.BusinessException;
import com.flightwebflux.model.Airline;
import com.flightwebflux.model.Flight;
import com.flightwebflux.repository.AirlineRepository;
import com.flightwebflux.repository.FlightRepository;
import com.flightwebflux.serviceImpl.FlightServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    FlightRepository flightRepository;

    @Mock
    AirlineRepository airlineRepository;

    @InjectMocks
    FlightServiceImpl flightService;

    private Airline sampleAirline;
    private Flight sampleFlight;
    private FlightRequest validRequest;

    @BeforeEach
    void setup() {
        sampleAirline = new Airline();
        sampleAirline.setAirlineId("AL-1");
        sampleAirline.setAirlineName("TestAir");

        sampleFlight = new Flight();
        sampleFlight.setFlightId("F-1");
        sampleFlight.setAirlineId(sampleAirline.getAirlineId());
        sampleFlight.setSource("AAA");
        sampleFlight.setDestination("BBB");
        sampleFlight.setDepartureDateTime(LocalDateTime.now().plusDays(1));
        sampleFlight.setArrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        sampleFlight.setTotalSeats(100);
        sampleFlight.setAvailableSeats(100);
        sampleFlight.setPrice(500.0);
        sampleFlight.setFlightNo("TA-123");

        validRequest = new FlightRequest();
        validRequest.setAirlineName("TestAir");
        validRequest.setSource("AAA");
        validRequest.setDestination("BBB");
        validRequest.setDepartureDateTime(sampleFlight.getDepartureDateTime());
        validRequest.setArrivalDateTime(sampleFlight.getArrivalDateTime());
        validRequest.setTotalSeats(100);
        validRequest.setPrice(500.0);
    }

    @Test
    void addFlights_success_newAirlineAndFlightSaved() {

        when(airlineRepository.findByAirlineName("TestAir")).thenReturn(Mono.empty());
        Airline savedAirline = new Airline();
        savedAirline.setAirlineId("AL-NEW");
        savedAirline.setAirlineName("TestAir");
        when(airlineRepository.save(any(Airline.class))).thenReturn(Mono.just(savedAirline));


        when(flightRepository.findByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
                eq("AL-NEW"), eq("AAA"), eq("BBB"), any(LocalDateTime.class)))
            .thenReturn(Mono.empty());


        Flight savedFlight = new Flight();
        savedFlight.setFlightId("F-NEW");
        savedFlight.setAirlineId("AL-NEW");
        when(flightRepository.save(any(Flight.class))).thenReturn(Mono.just(savedFlight));

        StepVerifier.create(flightService.addFlights(validRequest))
                .expectNextMatches(f -> "F-NEW".equals(f.getFlightId()) && "AL-NEW".equals(f.getAirlineId()))
                .verifyComplete();
    }

    @Test
    void addFlights_existingFlight_shouldError() {
        // Airline exists
        when(airlineRepository.findByAirlineName("TestAir")).thenReturn(Mono.just(sampleAirline));


        when(flightRepository.findByAirlineIdAndSourceAndDestinationAndDepartureDateTime(
                eq(sampleAirline.getAirlineId()), eq("AAA"), eq("BBB"), any(LocalDateTime.class)))
            .thenReturn(Mono.just(sampleFlight));

        StepVerifier.create(flightService.addFlights(validRequest))
                .expectErrorMatches(err -> err instanceof BusinessException
                        && err.getMessage().toLowerCase().contains("flight already exists"))
                .verify();
    }

    @Test
    void addFlights_invalidTimes_shouldError() {
        FlightRequest bad = new FlightRequest();
        bad.setAirlineName("A");
        bad.setSource("S");
        bad.setDestination("D");
        bad.setDepartureDateTime(LocalDateTime.now().plusDays(2));
        bad.setArrivalDateTime(LocalDateTime.now().plusDays(1)); // arrival before departure
        bad.setTotalSeats(10);
        bad.setPrice(100.0);

        StepVerifier.create(flightService.addFlights(bad))
                .expectErrorMatches(err -> err instanceof BusinessException
                        && err.getMessage().toLowerCase().contains("arrival time"))
                .verify();
    }

    @Test
    void searchFlights_bySourceDestination_returnsMappedResponse() {


        validRequest.setAirlineName(null); 

        when(flightRepository.findBySourceIgnoreCaseAndDestinationIgnoreCase("AAA", "BBB"))
                .thenReturn(Flux.just(sampleFlight));

        when(airlineRepository.findById(sampleFlight.getAirlineId()))
                .thenReturn(Mono.just(sampleAirline));

        StepVerifier.create(flightService.searchFlights(validRequest))
                .expectNextMatches(fr ->
                        fr.getAirlineName().equals("TestAir") &&
                        fr.getSource().equals("AAA") &&
                        fr.getDestination().equals("BBB"))
                .verifyComplete();
    }


    @Test
    void searchFlights_byAirlineName_returnsMappedResponse() {
        // set airlineName on request
        FlightRequest request2 = new FlightRequest();
        request2.setAirlineName("TestAir");

        when(airlineRepository.findByAirlineName("TestAir")).thenReturn(Mono.just(sampleAirline));
        when(flightRepository.findByAirlineIdIgnoreCase(sampleAirline.getAirlineId()))
                .thenReturn(Flux.just(sampleFlight));
        when(airlineRepository.findById(sampleFlight.getAirlineId())).thenReturn(Mono.just(sampleAirline));

        StepVerifier.create(flightService.searchFlights(request2))
                .expectNextMatches(fr -> "TestAir".equals(fr.getAirlineName()) && fr.getFlightNo() != null)
                .verifyComplete();
    }
}

