package com.hotel.hotelbooking.service;

import com.hotel.hotelbooking.entity.Room;
import com.hotel.hotelbooking.exception.ResourceNotFoundException;
import com.hotel.hotelbooking.repository.RoomRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + id));
    }

    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom(Long id, Room roomDetails) {
        Room room = getRoomById(id);
        room.setRoomNumber(roomDetails.getRoomNumber());
        room.setRoomType(roomDetails.getRoomType());
        room.setPricePerNight(roomDetails.getPricePerNight());
        room.setAvailable(roomDetails.isAvailable());
        room.setDescription(roomDetails.getDescription());
        return roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        getRoomById(id); // throws 404 if not found
        roomRepository.deleteById(id);
    }

    public List<Room> getAvailableRoomsByType(String type) {
        return roomRepository.findAvailableRoomsByType(type);
    }

    public List<Room> getRoomsByMaxPrice(Double maxPrice) {
        return roomRepository.findRoomsByMaxPrice(maxPrice);
    }

    public List<Room> getAllAvailableRooms() {
        return roomRepository.findAllAvailableRooms();
    }
}
