package com.hotel.hotelbooking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Room number is required")
    @Column(name = "room_number", unique = true, nullable = false)
    private String roomNumber;

    @NotBlank(message = "Room type is required")
    @Column(name = "room_type", nullable = false)
    private String roomType;  // SINGLE, DOUBLE, SUITE

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price_per_night", nullable = false)
    private Double pricePerNight;

    @Column(name = "available")
    private boolean available = true;

    @Column(name = "description")
    private String description;
}
