package com.hotel.hotelbooking.service;

import com.hotel.hotelbooking.dto.BookingRequest;
import com.hotel.hotelbooking.entity.*;
import com.hotel.hotelbooking.exception.*;
import com.hotel.hotelbooking.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private RoomRepository    roomRepository;
    @Mock private UserRepository    userRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room          room;
    private User          user;
    private BookingRequest bookingRequest;

    @BeforeEach
    void setUp() {
        room = new Room(1L, "101", "SINGLE", 1500.0, true, "Sea view");

        user = User.builder()
                .id(1L)
                .fullName("Ravi Kumar")
                .email("ravi@email.com")
                .phone("9876543210")
                .role(User.Role.USER)
                .password("encoded")
                .build();

        bookingRequest = new BookingRequest();
        bookingRequest.setRoomId(1L);
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(1));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(3));
    }

    // ---- createBooking success ----

    @Test
    void testCreateBooking_Success() {
        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.countOverlapping(any(), any(), any())).thenReturn(0L);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Booking result = bookingService.createBooking(bookingRequest, "ravi@email.com");

        assertNotNull(result);
        assertEquals(Booking.BookingStatus.CONFIRMED, result.getStatus());
        assertEquals(3000.0, result.getTotalAmount()); // 2 nights * 1500
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    // ---- createBooking — room not found ----

    @Test
    void testCreateBooking_RoomNotFound_ThrowsException() {
        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingRequest, "ravi@email.com"));
    }

    // ---- createBooking — user not found ----

    @Test
    void testCreateBooking_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@email.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(bookingRequest, "notfound@email.com"));
    }

    // ---- createBooking — room unavailable ----

    @Test
    void testCreateBooking_RoomUnavailable_ThrowsException() {
        room.setAvailable(false);
        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(bookingRequest, "ravi@email.com"));
    }

    // ---- createBooking — overlapping dates ----

    @Test
    void testCreateBooking_OverlappingDates_ThrowsException() {
        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.countOverlapping(any(), any(), any())).thenReturn(1L);

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(bookingRequest, "ravi@email.com"));
    }

    // ---- createBooking — checkout before checkin ----

    @Test
    void testCreateBooking_InvalidDates_ThrowsException() {
        bookingRequest.setCheckInDate(LocalDate.now().plusDays(3));
        bookingRequest.setCheckOutDate(LocalDate.now().plusDays(1));

        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(BookingConflictException.class,
                () -> bookingService.createBooking(bookingRequest, "ravi@email.com"));
    }

    // ---- cancelBooking success ----

    @Test
    void testCancelBooking_Success() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Booking result = bookingService.cancelBooking(1L, "ravi@email.com", false);

        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());
        assertTrue(room.isAvailable());
    }

    // ---- cancelBooking — wrong user ----

    @Test
    void testCancelBooking_WrongUser_ThrowsException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class,
                () -> bookingService.cancelBooking(1L, "other@email.com", false));
    }

    // ---- cancelBooking — admin can cancel any booking ----

    @Test
    void testCancelBooking_AdminCanCancelAny() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        Booking result = bookingService.cancelBooking(1L, "admin@hotel.com", true);

        assertEquals(Booking.BookingStatus.CANCELLED, result.getStatus());
    }

    // ---- cancelBooking — already cancelled ----

    @Test
    void testCancelBooking_AlreadyCancelled_ThrowsException() {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class,
                () -> bookingService.cancelBooking(1L, "ravi@email.com", false));
    }

    // ---- getMyBookings ----

    @Test
    void testGetMyBookings_Success() {
        when(userRepository.findByEmail("ravi@email.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findByUserId(1L)).thenReturn(java.util.List.of());

        var result = bookingService.getMyBookings("ravi@email.com");

        assertNotNull(result);
        verify(bookingRepository, times(1)).findByUserId(1L);
    }

    // ---- getBookingById not found ----

    @Test
    void testGetBookingById_NotFound_ThrowsException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById(99L));
    }
}