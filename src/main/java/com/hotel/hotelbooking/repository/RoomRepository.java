package com.hotel.hotelbooking.repository;

import com.hotel.hotelbooking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    // JPQL Custom Query 1 — Available rooms by type
    @Query("SELECT r FROM Room r WHERE r.available = true AND r.roomType = :type ORDER BY r.pricePerNight ASC")
    List<Room> findAvailableRoomsByType(@Param("type") String type);

    // JPQL Custom Query 2 — Rooms under a max price (available only)
    @Query("SELECT r FROM Room r WHERE r.pricePerNight <= :maxPrice AND r.available = true ORDER BY r.pricePerNight ASC")
    List<Room> findRoomsByMaxPrice(@Param("maxPrice") Double maxPrice);

    // JPQL Custom Query 3 — All available rooms sorted by price
    @Query("SELECT r FROM Room r WHERE r.available = true ORDER BY r.pricePerNight ASC")
    List<Room> findAllAvailableRooms();
}
