package com.flightwebflux.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.flightwebflux.enums.BookingStatus;
import com.flightwebflux.model.Passenger;

import lombok.Data;


@Data
public class BookingResponse {

    private String pnr;
    private String bookerEmailId;
    private BookingStatus status;
    private Double totalAmount;
    private LocalDateTime bookingDateTime;

    private String flightNo;
    private String airlineName;
    private String source;
    private String destination;

    private List<Passenger> passengers;
}
