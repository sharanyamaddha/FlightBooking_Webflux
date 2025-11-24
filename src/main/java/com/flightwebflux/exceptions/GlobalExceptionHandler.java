package com.flightwebflux.exceptions;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, String>> handleBusinessException(BusinessException ex) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage()); 
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)  
                .body(body);
    }
	
	@ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
	       Map<String, String> body = new LinkedHashMap<>();
	        body.put("message", ex.getMessage()); 
	        return ResponseEntity
	                .status(409)  
	                .body(body);

    }
	
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
    	Map<String, String> body = new LinkedHashMap<>();
        body.put("message", ex.getMessage()); 
        return ResponseEntity
                .status(400)  
                .body(body);

    }
	
}
