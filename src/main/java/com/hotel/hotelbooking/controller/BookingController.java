package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.dto.BookingRequest;
import com.hotel.hotelbooking.entity.Booking;
import com.hotel.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelMyBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                bookingService.cancelBooking(id, userDetails.getUsername(), false));
    }
}