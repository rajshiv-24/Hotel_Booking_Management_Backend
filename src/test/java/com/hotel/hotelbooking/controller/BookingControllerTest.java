package com.hotel.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotel.hotelbooking.dto.BookingRequest;
import com.hotel.hotelbooking.entity.*;
import com.hotel.hotelbooking.exception.*;
import com.hotel.hotelbooking.security.JwtService;
import com.hotel.hotelbooking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(GlobalExceptionHandler.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private Booking booking;
    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        room = new Room(1L, "101", "SINGLE", 1500.0, true, "Sea view");

        user = User.builder()
                .id(1L)
                .fullName("Ravi Kumar")
                .email("ravi@email.com")
                .phone("9876543210")
                .role(User.Role.USER)
                .password("encoded")
                .build();

        booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setCheckInDate(LocalDate.now().plusDays(1));
        booking.setCheckOutDate(LocalDate.now().plusDays(3));
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setTotalAmount(3000.0);
    }

    // ---- POST /api/user/bookings ----

    @Test
    @WithMockUser(username = "ravi@email.com", roles = "USER")
    void testCreateBooking_Returns201() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class), eq("ravi@email.com")))
                .thenReturn(booking);

        BookingRequest request = new BookingRequest();
        request.setRoomId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/user/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(3000.0));
    }

    @Test
    @WithMockUser(username = "ravi@email.com", roles = "USER")
    void testCreateBooking_Conflict_Returns409() throws Exception {
        when(bookingService.createBooking(any(BookingRequest.class), eq("ravi@email.com")))
                .thenThrow(new BookingConflictException("Room is already booked for the selected dates."));

        BookingRequest request = new BookingRequest();
        request.setRoomId(1L);
        request.setCheckInDate(LocalDate.now().plusDays(1));
        request.setCheckOutDate(LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/user/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Room is already booked for the selected dates."));
    }

    // ---- GET /api/user/bookings ----

    @Test
    @WithMockUser(username = "ravi@email.com", roles = "USER")
    void testGetMyBookings_Returns200() throws Exception {
        when(bookingService.getMyBookings("ravi@email.com")).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/user/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ---- PUT /api/user/bookings/{id}/cancel ----

    @Test
    @WithMockUser(username = "ravi@email.com", roles = "USER")
    void testCancelMyBooking_Returns200() throws Exception {
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        when(bookingService.cancelBooking(1L, "ravi@email.com", false)).thenReturn(booking);

        mockMvc.perform(put("/api/user/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "ravi@email.com", roles = "USER")
    void testCancelMyBooking_NotFound_Returns404() throws Exception {
        when(bookingService.cancelBooking(eq(99L), eq("ravi@email.com"), eq(false)))
                .thenThrow(new ResourceNotFoundException("Booking not found with id: 99"));

        mockMvc.perform(put("/api/user/bookings/99/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Booking not found with id: 99"));
    }

    // ---- Admin cancel — PUT /api/admin/bookings/{id}/cancel ----

    @Test
    @WithMockUser(username = "admin@hotel.com", roles = "ADMIN")
    void testAdminCancelBooking_Returns200() throws Exception {
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        when(bookingService.cancelBooking(1L, "admin@hotel.com", true)).thenReturn(booking);

        mockMvc.perform(put("/api/admin/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}