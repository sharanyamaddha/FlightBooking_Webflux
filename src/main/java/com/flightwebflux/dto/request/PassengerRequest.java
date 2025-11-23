package com.flightwebflux.dto.request;

import com.flightwebflux.enums.Gender;
import com.flightwebflux.enums.MealType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PassengerRequest {


	@NotEmpty
	String name;
	
	@NotNull
	Gender gender;
	
	@NotNull
	int age;
	
	@NotBlank
	String seatNo;
	
	@NotNull
	MealType mealType;
}
