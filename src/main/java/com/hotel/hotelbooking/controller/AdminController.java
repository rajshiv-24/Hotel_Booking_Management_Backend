package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.entity.*;
import com.hotel.hotelbooking.repository.UserRepository;
import com.hotel.hotelbooking.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final RoomService    roomService;
    private final BookingService bookingService;
    private final UserRepository userRepository;

    // ---- Room management ----
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@Valid @RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(room));
    }

    @PutMapping("/rooms/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
        return ResponseEntity.ok(roomService.updateRoom(id, room));
    }

    @DeleteMapping("/rooms/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Booking management ----
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(bookingService.cancelBooking(id, userDetails.getUsername(), true));
    }

    // ---- Customer management ----
    @GetMapping("/customers")
    public ResponseEntity<List<User>> getAllCustomers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.USER)
                .toList()
        );
    }

    @DeleteMapping("/customers/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}