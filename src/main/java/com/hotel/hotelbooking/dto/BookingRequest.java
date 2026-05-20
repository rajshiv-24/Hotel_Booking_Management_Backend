package com.hotel.hotelbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingRequest {
    @NotNull private Long roomId;
    @NotNull private LocalDate checkInDate;
    @NotNull private LocalDate checkOutDate;
}