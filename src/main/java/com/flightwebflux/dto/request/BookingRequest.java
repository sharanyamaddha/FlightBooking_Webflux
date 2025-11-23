package com.flightwebflux.dto.request;

import java.util.List;

import com.flightwebflux.enums.TripType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingRequest {

	@NotBlank
	String flightId;
	
	@NotBlank  @Email
	String bookerEmailId;
	
	@NotNull
	TripType tripType;
	
	@Size(min=1,message="At least one passenger is required")
	@Valid
	List<PassengerRequest> passengers;
}
