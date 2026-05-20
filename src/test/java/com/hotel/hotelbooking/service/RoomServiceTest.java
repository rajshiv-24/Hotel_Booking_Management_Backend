package com.hotel.hotelbooking.service;

import com.hotel.hotelbooking.entity.Room;
import com.hotel.hotelbooking.exception.ResourceNotFoundException;
import com.hotel.hotelbooking.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomService roomService;

    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        room1 = new Room(1L, "101", "SINGLE", 1500.0, true, "Sea view room");
        room2 = new Room(2L, "102", "DOUBLE", 2500.0, true, "Garden view room");
    }

    // ---- getAllRooms ----

    @Test
    void testGetAllRooms_ReturnsList() {
        when(roomRepository.findAll()).thenReturn(Arrays.asList(room1, room2));

        List<Room> result = roomService.getAllRooms();

        assertEquals(2, result.size());
        verify(roomRepository, times(1)).findAll();
    }

    // ---- getRoomById ----

    @Test
    void testGetRoomById_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));

        Room result = roomService.getRoomById(1L);

        assertNotNull(result);
        assertEquals("101", result.getRoomNumber());
        assertEquals("SINGLE", result.getRoomType());
    }

    @Test
    void testGetRoomById_NotFound_ThrowsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.getRoomById(99L));
        verify(roomRepository, times(1)).findById(99L);
    }

    // ---- createRoom ----

    @Test
    void testCreateRoom_Success() {
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        Room saved = roomService.createRoom(room1);

        assertNotNull(saved);
        assertEquals("101", saved.getRoomNumber());
        verify(roomRepository, times(1)).save(room1);
    }

    // ---- updateRoom ----

    @Test
    void testUpdateRoom_Success() {
        Room updatedDetails = new Room(null, "101", "SUITE", 5000.0, true, "Updated description");
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        when(roomRepository.save(any(Room.class))).thenReturn(room1);

        Room result = roomService.updateRoom(1L, updatedDetails);

        assertNotNull(result);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void testUpdateRoom_NotFound_ThrowsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> roomService.updateRoom(99L, room1));
    }

    // ---- deleteRoom ----

    @Test
    void testDeleteRoom_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room1));
        doNothing().when(roomRepository).deleteById(1L);

        assertDoesNotThrow(() -> roomService.deleteRoom(1L));
        verify(roomRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteRoom_NotFound_ThrowsException() {
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roomService.deleteRoom(99L));
        verify(roomRepository, never()).deleteById(any());
    }

    // ---- getAvailableRoomsByType ----

    @Test
    void testGetAvailableRoomsByType_ReturnsList() {
        when(roomRepository.findAvailableRoomsByType("SINGLE")).thenReturn(List.of(room1));

        List<Room> result = roomService.getAvailableRoomsByType("SINGLE");

        assertEquals(1, result.size());
        assertEquals("SINGLE", result.get(0).getRoomType());
    }

    // ---- getRoomsByMaxPrice ----

    @Test
    void testGetRoomsByMaxPrice_ReturnsList() {
        when(roomRepository.findRoomsByMaxPrice(2000.0)).thenReturn(List.of(room1));

        List<Room> result = roomService.getRoomsByMaxPrice(2000.0);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getPricePerNight() <= 2000.0);
    }
}
