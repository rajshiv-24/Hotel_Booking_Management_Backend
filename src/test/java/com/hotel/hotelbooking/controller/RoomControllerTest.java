package com.hotel.hotelbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.hotelbooking.entity.Room;
import com.hotel.hotelbooking.exception.GlobalExceptionHandler;
import com.hotel.hotelbooking.exception.ResourceNotFoundException;
import com.hotel.hotelbooking.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoomController.class)
@Import(GlobalExceptionHandler.class)
public class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomService roomService;

    @Autowired
    private ObjectMapper objectMapper;

    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        room1 = new Room(1L, "101", "SINGLE", 1500.0, true,  "Sea view");
        room2 = new Room(2L, "102", "DOUBLE", 2500.0, false, "Garden view");
    }

    // ---- GET /api/rooms ----

    @Test
    void testGetAllRooms_Returns200() throws Exception {
        when(roomService.getAllRooms()).thenReturn(Arrays.asList(room1, room2));

        mockMvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].roomNumber").value("101"))
                .andExpect(jsonPath("$[1].roomNumber").value("102"));
    }

    // ---- GET /api/rooms/{id} ----

    @Test
    void testGetRoomById_Returns200() throws Exception {
        when(roomService.getRoomById(1L)).thenReturn(room1);

        mockMvc.perform(get("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.roomType").value("SINGLE"))
                .andExpect(jsonPath("$.pricePerNight").value(1500.0));
    }

    @Test
    void testGetRoomById_NotFound_Returns404() throws Exception {
        when(roomService.getRoomById(99L))
                .thenThrow(new ResourceNotFoundException("Room not found with id: 99"));

        mockMvc.perform(get("/api/rooms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Room not found with id: 99"));
    }

    // ---- POST /api/rooms ----

    @Test
    void testCreateRoom_Returns201() throws Exception {
        when(roomService.createRoom(any(Room.class))).thenReturn(room1);

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(room1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomNumber").value("101"))
                .andExpect(jsonPath("$.roomType").value("SINGLE"));
    }

    @Test
    void testCreateRoom_InvalidData_Returns400() throws Exception {
        // Missing required fields — roomNumber is blank
        Room invalid = new Room(null, "", "SINGLE", 1500.0, true, "desc");

        mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ---- PUT /api/rooms/{id} ----

    @Test
    void testUpdateRoom_Returns200() throws Exception {
        Room updated = new Room(1L, "101", "SUITE", 5000.0, true, "Updated");
        when(roomService.updateRoom(eq(1L), any(Room.class))).thenReturn(updated);

        mockMvc.perform(put("/api/rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomType").value("SUITE"))
                .andExpect(jsonPath("$.pricePerNight").value(5000.0));
    }

    // ---- DELETE /api/rooms/{id} ----

    @Test
    void testDeleteRoom_Returns204() throws Exception {
        doNothing().when(roomService).deleteRoom(1L);

        mockMvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isNoContent());

        verify(roomService, times(1)).deleteRoom(1L);
    }

    @Test
    void testDeleteRoom_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Room not found with id: 99"))
                .when(roomService).deleteRoom(99L);

        mockMvc.perform(delete("/api/rooms/99"))
                .andExpect(status().isNotFound());
    }

    // ---- GET /api/rooms/available ----

    @Test
    void testGetAvailableRooms_Returns200() throws Exception {
        when(roomService.getAllAvailableRooms()).thenReturn(List.of(room1));

        mockMvc.perform(get("/api/rooms/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    // ---- GET /api/rooms/filter?maxPrice=2000 ----

    @Test
    void testGetRoomsByMaxPrice_Returns200() throws Exception {
        when(roomService.getRoomsByMaxPrice(2000.0)).thenReturn(List.of(room1));

        mockMvc.perform(get("/api/rooms/filter").param("maxPrice", "2000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pricePerNight").value(1500.0));
    }
}
