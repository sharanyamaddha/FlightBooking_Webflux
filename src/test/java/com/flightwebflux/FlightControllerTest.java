package com.flightwebflux;



import com.flightwebflux.dto.request.FlightRequest;
import com.flightwebflux.dto.response.FlightResponse;
import com.flightwebflux.model.Flight;
import com.flightwebflux.service.FlightService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class FlightControllerTest {

    @Autowired
    private WebTestClient webClient;

    @MockBean
    private FlightService flightService;

    @Test
    void addFlights_returnsCreatedAndFlightId() {
        Flight saved = new Flight();
        saved.setFlightId("F-100");

        when(flightService.addFlights(any(FlightRequest.class)))
                .thenReturn(Mono.just(saved));

        FlightRequest req = new FlightRequest();
        req.setAirlineName("TestAir");
        req.setSource("AAA");
        req.setDestination("BBB");
        req.setDepartureDateTime(LocalDateTime.now().plusDays(1));
        req.setArrivalDateTime(LocalDateTime.now().plusDays(1).plusHours(2));
        req.setTotalSeats(100);
        req.setPrice(500.0);

        webClient.post().uri("/flight/add")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(String.class)
                .isEqualTo("F-100");
    }

    @Test
    void searchFlights_returnsFlightResponseList() {
        FlightResponse fr = new FlightResponse();
        fr.setFlightNo("TA-111");
        fr.setAirlineName("TestAir");
        fr.setSource("AAA");
        fr.setDestination("BBB");

        when(flightService.searchFlights(any(FlightRequest.class)))
                .thenReturn(Flux.just(fr));

        FlightRequest req = new FlightRequest();
        req.setSource("AAA");
        req.setDestination("BBB");

        webClient.post().uri("/flight/search")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FlightResponse.class)
                .hasSize(1);
    }
}
