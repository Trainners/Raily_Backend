package io.trainners.raily_backend.domain.train.dto;

import io.trainners.raily_backend.domain.seat.model.SeatOption;

import java.util.List;

public record SeatResponse(
        String carNumber,
        String seatNumber,
        List<Boolean> availabilityBySegment
) {
    public static SeatResponse from(SeatOption seatOption) {
        return new SeatResponse(
                seatOption.getCarNumber(),
                seatOption.getSeatNumber(),
                seatOption.getAvailabilityBySegment()
        );
    }
}