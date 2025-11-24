**Flight Booking System – Spring WebFlux & MongoDB**

This project is a reactive Flight Booking System built using Spring WebFlux and MongoDB.
The goal of the application is to manage flights and bookings in a non-blocking, high-performance way, handling multiple requests efficiently using reactive streams (Mono/Flux).
It includes features like flight management, seat validation, passenger handling, and booking cancellation logic, all implemented with proper exception handling and reactive programming principles.

API Overview

POST /api/v1.0/flight/airline/inventory/add

Adds the inventory or schedule for an existing airline.
This API is used to create or update flight schedules, including details like available seats, timings, and other flight metadata.

POST /api/v1.0/flight/search

Searches for flights based on criteria such as origin, destination, and airline name.
This endpoint helps users find all available flights matching their search filters.


POST /api/v1.0/flight/booking/{flightid}

Creates a new ticket booking for the given flight ID.
It handles validations such as:

Checking flight availability

Verifying seat count

Saving passenger details

Preventing double bookings

GET /api/v1.0/flight/ticket/{pnr}

Retrieves ticket details using the PNR number.
This API returns booking information like passenger details, flight info, and booking status.

GET /api/v1.0/flight/booking/history/{emailId}

Fetches the booking history for a specific user based on their email ID.
Helps users view all past and current bookings.

DELETE /api/v1.0/flight/booking/cancel/{pnr}

Cancels a booked ticket using the PNR.
Before cancellation, the system verifies the booking status and ensures that the request follows the cancellation rules .
