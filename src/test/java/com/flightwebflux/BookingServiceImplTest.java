package com.flightwebflux;

import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.request.PassengerRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.model.Booking;
import com.flightwebflux.model.Flight;
import com.flightwebflux.model.Passenger;
import com.flightwebflux.repository.BookingRepository;
import com.flightwebflux.repository.FlightRepository;
import com.flightwebflux.repository.PassengerRepository;
import com.flightwebflux.serviceImpl.BookingServiceImpl;
import com.flightwebflux.enums.BookingStatus;
import com.flightwebflux.exceptions.BadRequestException;
import com.flightwebflux.exceptions.ConflictException;
import com.flightwebflux.exceptions.BusinessException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    FlightRepository flightRepository;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    PassengerRepository passengerRepository;

    @InjectMocks
    BookingServiceImpl bookingService;

    private Flight sampleFlight;

    @BeforeEach
    void setup() {
        sampleFlight = new Flight();
        sampleFlight.setFlightId("FL-1");
        sampleFlight.setAirlineName("TestAir");
        sampleFlight.setAvailableSeats(5);
        sampleFlight.setPrice(100.0);
        sampleFlight.setSource("A");
        sampleFlight.setDestination("B");
    }

    @Test
    void createBooking_successfulFlow() {
        
        PassengerRequest p1 = new PassengerRequest();
        p1.setName("Alice");
        p1.setSeatNo("1A");
        PassengerRequest p2 = new PassengerRequest();
        p2.setName("Bob");
        p2.setSeatNo("1B");

        BookingRequest req = new BookingRequest();
        req.setBookerEmailId("a@b.com");
        req.setPassengers(List.of(p1, p2));
        req.setTripType(null);

        Booking savedBooking = new Booking();
        savedBooking.setPnr("PNR-ABC12345");
        savedBooking.setFlightId(sampleFlight.getFlightId());
        savedBooking.setSeatsBooked(2);
        savedBooking.setBookingDateTime(LocalDateTime.now());
        savedBooking.setStatus(BookingStatus.BOOKED);

        
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(sampleFlight));
        // no seat conflicts
        when(passengerRepository.findByFlightIdAndSeatNoIn(eq("FL-1"), anyList())).thenReturn(Flux.empty());
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(savedBooking));
        when(flightRepository.save(any(Flight.class))).thenReturn(Mono.just(sampleFlight));
        // passengerRepository.saveAll returns Flux of passengers 
        when(passengerRepository.saveAll(anyIterable())).thenReturn(Flux.fromIterable(List.of(new Passenger(), new Passenger())));

        StepVerifier.create(bookingService.createBooking("FL-1", req))
                .expectNextMatches(b -> b.getPnr() != null && b.getSeatsBooked() == 2)
                .verifyComplete();

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_notEnoughSeats_shouldError() {
        sampleFlight.setAvailableSeats(1); // only 1 seat
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(sampleFlight));

        PassengerRequest p1 = new PassengerRequest(); p1.setSeatNo("1A");
        PassengerRequest p2 = new PassengerRequest(); p2.setSeatNo("1B");
        BookingRequest req = new BookingRequest();
        req.setPassengers(List.of(p1, p2));
        req.setBookerEmailId("x@y.com");

        StepVerifier.create(bookingService.createBooking("FL-1", req))
                .expectErrorMatches(err -> err instanceof BusinessException
                        && err.getMessage().toLowerCase().contains("not enough seats"))
                .verify();
    }

    @Test
    void createBooking_seatConflict_shouldError() {
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(sampleFlight));

        PassengerRequest p1 = new PassengerRequest(); p1.setSeatNo("1A");
        BookingRequest req = new BookingRequest();
        req.setPassengers(List.of(p1));
        req.setBookerEmailId("u@v.com");

        // passengerRepository returns an existing passenger with same seat
        Passenger existing = new Passenger();
        existing.setSeatNo("1A");
        when(passengerRepository.findByFlightIdAndSeatNoIn(eq("FL-1"), anyList())).thenReturn(Flux.just(existing));

        StepVerifier.create(bookingService.createBooking("FL-1", req))
                .expectErrorMatches(err -> err instanceof BusinessException
                        && err.getMessage().toLowerCase().contains("seat"))
                .verify();
    }

    @Test
    void getBookingByPnr_mapsToBookingResponse() {
        Booking booking = new Booking();
        booking.setPnr("PNR-XYZ");
        booking.setFlightId("FL-1");
        booking.setBookerEmailId("a@b.com");
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setTotalAmount(200.0);
        booking.setStatus(BookingStatus.BOOKED);

        Flight flight = sampleFlight;
        flight.setFlightId("FL-1");
        Passenger p = new Passenger();
        p.setName("Alice");
        p.setSeatNo("1A");

        when(bookingRepository.findByPnr("PNR-XYZ")).thenReturn(Mono.just(booking));
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(flight));
        when(passengerRepository.findByPnr("PNR-XYZ")).thenReturn(Flux.just(p));

        StepVerifier.create(bookingService.getBookingByPnr("PNR-XYZ"))
                .expectNextMatches(resp -> {
                    BookingResponse br = (BookingResponse) resp;
                    return "PNR-XYZ".equals(br.getPnr())
                            && br.getPassengers() != null
                            && br.getPassengers().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void cancelBooking_within24Hours_succeeds() {
        Booking booking = new Booking();
        booking.setPnr("PNR-C1");
        booking.setFlightId("FL-1");
        booking.setBookingDateTime(LocalDateTime.now().minusHours(2)); // within 24h
        booking.setStatus(BookingStatus.BOOKED);

        Flight flight = sampleFlight;
        flight.setAvailableSeats(3);

        when(bookingRepository.findByPnr("PNR-C1")).thenReturn(Mono.just(booking));
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(flight));
        when(bookingRepository.save(any(Booking.class))).thenReturn(Mono.just(booking));
        when(passengerRepository.countByPnr("PNR-C1")).thenReturn(Mono.just(2L));
        when(flightRepository.save(any(Flight.class))).thenReturn(Mono.just(flight));

        StepVerifier.create(bookingService.cancelBooking("PNR-C1"))
                .expectNextMatches(msg -> msg.toLowerCase().contains("cancelled"))
                .verifyComplete();
    }

    @Test
    void cancelBooking_after24Hours_shouldError() {
        Booking booking = new Booking();
        booking.setPnr("PNR-OLD");
        booking.setFlightId("FL-1");
        booking.setBookingDateTime(LocalDateTime.now().minusHours(30)); // older than 24h
        booking.setStatus(BookingStatus.BOOKED);

        
        when(bookingRepository.findByPnr("PNR-OLD")).thenReturn(Mono.just(booking));
        
        when(flightRepository.findById("FL-1")).thenReturn(Mono.just(sampleFlight));

        StepVerifier.create(bookingService.cancelBooking("PNR-OLD"))
                .expectErrorMatches(err -> err instanceof BadRequestException
                        && err.getMessage().toLowerCase().contains("cancellation"))
                .verify();
    }


    @Test
    void cancelBooking_alreadyCancelled_shouldConflict() {
        Booking booking = new Booking();
        booking.setPnr("PNR-X");
        booking.setFlightId("FL-1");
        booking.setBookingDateTime(LocalDateTime.now().minusHours(1));
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findByPnr("PNR-X")).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.cancelBooking("PNR-X"))
                .expectErrorMatches(err -> err instanceof ConflictException
                        && err.getMessage().toLowerCase().contains("already cancelled"))
                .verify();
    }
}

