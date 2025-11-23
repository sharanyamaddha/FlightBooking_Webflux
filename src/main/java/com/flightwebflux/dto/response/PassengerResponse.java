package com.flightwebflux.dto.response;

import com.flightwebflux.enums.Gender;
import com.flightwebflux.enums.MealType;

import lombok.Data;

@Data
public class PassengerResponse {

	String name;
	int age;
	Gender gender;
	String seatNo;
	MealType mealType;
	
}
