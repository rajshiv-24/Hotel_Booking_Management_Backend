package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.entity.Room;
import com.hotel.hotelbooking.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAllAvailableRooms());
    }

    @GetMapping("/all")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/available/type")
    public ResponseEntity<List<Room>> getAvailableByType(@RequestParam String type) {
        return ResponseEntity.ok(roomService.getAvailableRoomsByType(type));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Room>> getRoomsByMaxPrice(@RequestParam Double maxPrice) {
        return ResponseEntity.ok(roomService.getRoomsByMaxPrice(maxPrice));
    }
}