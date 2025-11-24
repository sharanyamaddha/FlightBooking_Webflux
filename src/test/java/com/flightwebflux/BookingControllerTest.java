package com.flightwebflux;


import com.flightwebflux.dto.request.BookingRequest;
import com.flightwebflux.dto.request.PassengerRequest;
import com.flightwebflux.dto.response.BookingResponse;
import com.flightwebflux.enums.Gender;
import com.flightwebflux.enums.MealType;

import com.flightwebflux.service.BookingService;
import com.flightwebflux.repository.AirlineRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BookingControllerTest {

    @Autowired
    private WebTestClient webClient;


    @Mock
    private BookingService bookingService;

    @Mock
    private AirlineRepository airlineRepository;

    @Test
    void createBooking_returnsCreated() {
        
        com.flightwebflux.model.Booking savedBooking = new com.flightwebflux.model.Booking();
        savedBooking.setPnr("PNR-T1");

       
        when(bookingService.createBooking(eq("FL-1"), any(BookingRequest.class)))
                .thenReturn(Mono.just(savedBooking));

        
        BookingRequest req = new BookingRequest();
        req.setBookerEmailId("user@example.com");
        
        req.setTripType(com.flightwebflux.enums.TripType.ONE_WAY); 
        PassengerRequest p = new PassengerRequest();
        p.setName("Alice");
        p.setGender(Gender.FEMALE);      
        p.setAge(28);          
        p.setSeatNo("1A");
        p.setMealType(MealType.VEG);


        req.setPassengers(List.of(p));

        webClient.post().uri("/booking/FL-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("PNR-T1");
    }


    @Test
    void getBookingByPnr_returnsBookingResponse() {
        BookingResponse resp = new BookingResponse();
        resp.setPnr("PNR-GET");
        resp.setBookerEmailId("user@example.com");

        when(bookingService.getBookingByPnr("PNR-GET")).thenReturn(Mono.just(resp));

        webClient.get().uri("/booking/PNR-GET")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.pnr").isEqualTo("PNR-GET");
    }

    @Test
    void getBookingHistory_returnsFlux() {
        BookingResponse r1 = new BookingResponse(); r1.setPnr("P1");
        BookingResponse r2 = new BookingResponse(); r2.setPnr("P2");

        when(bookingService.getBookingHistory("user@example.com")).thenReturn(Flux.just(r1, r2));

        webClient.get().uri("/booking/history/user@example.com")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookingResponse.class)
                .hasSize(2);
    }

    @Test
    void cancelBooking_returnsOkMessage() {
        when(bookingService.cancelBooking("PNR-C")).thenReturn(Mono.just("Booking cancelled successfully"));

        webClient.delete().uri("/booking/cancel/PNR-C")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Booking cancelled successfully");
    }
}
