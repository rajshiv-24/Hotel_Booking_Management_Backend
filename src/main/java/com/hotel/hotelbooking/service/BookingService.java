package com.hotel.hotelbooking.service;

import com.hotel.hotelbooking.dto.BookingRequest;
import com.hotel.hotelbooking.entity.*;
import com.hotel.hotelbooking.exception.*;
import com.hotel.hotelbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository    roomRepository;
    private final UserRepository    userRepository;

    public Booking createBooking(BookingRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + request.getRoomId()));

        if (!room.isAvailable()) {
            throw new BookingConflictException("Room " + room.getRoomNumber() + " is not available.");
        }

        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BookingConflictException("Check-out must be after check-in.");
        }

        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new BookingConflictException("Check-in date cannot be in the past.");
        }

        Long overlapping = bookingRepository.countOverlapping(
                room.getId(), request.getCheckInDate(), request.getCheckOutDate());
        if (overlapping > 0) {
            throw new BookingConflictException("Room already booked for selected dates.");
        }

        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalAmount(room.getPricePerNight() * nights);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        room.setAvailable(false);
        roomRepository.save(room);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getMyBookings(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return bookingRepository.findByUserId(user.getId());
    }
    
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    public Booking cancelBooking(Long id, String userEmail, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (!isAdmin && !booking.getUser().getEmail().equals(userEmail)) {
            throw new BookingConflictException("You can only cancel your own bookings.");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingConflictException("Booking is already cancelled.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.getRoom().setAvailable(true);
        roomRepository.save(booking.getRoom());
        return bookingRepository.save(booking);
    }
}