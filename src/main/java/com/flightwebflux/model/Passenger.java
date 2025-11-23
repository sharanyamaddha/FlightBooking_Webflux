package com.flightwebflux.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.flightwebflux.enums.Gender;
import com.flightwebflux.enums.MealType;
import com.flightwebflux.enums.TripType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {

	@Id
	String passengerId;
	
	 String flightId;
	 
	 String pnr;
	 
	 String name;
	 
		int age;
		
		Gender gender;
		
		String seatNo;
		
		MealType mealType;
}
