package com.hotel.hotelbooking.repository;

import com.hotel.hotelbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.checkInDate DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate >= :start AND b.checkOutDate <= :end")
    List<Booking> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.id = :roomId " +
           "AND b.status = 'CONFIRMED' " +
           "AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)")
    Long countOverlapping(@Param("roomId") Long roomId,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b FROM Booking b WHERE b.status = :status ORDER BY b.checkInDate DESC")
    List<Booking> findByStatus(@Param("status") Booking.BookingStatus status);
}