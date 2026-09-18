package io.trainners.raily_backend.domain.train.dto;

import io.trainners.raily_backend.domain.seat.model.SeatOption;

public record SeatResponse(
        String carNumber,
        String seatNumber,
        boolean seatableNow,
        int initialContiguousRun,
        int longestContiguousRun,
        int availableSegmentCount,
        boolean coversWholeJourney
) {
    public static SeatResponse from(SeatOption seatOption) {
        return new SeatResponse(
                seatOption.getCarNumber(),
                seatOption.getSeatNumber(),
                seatOption.isSeatableNow(),
                seatOption.getInitialContiguousRun(),
                seatOption.getLongestContiguousRun(),
                seatOption.getAvailableSegmentCount(),
                seatOption.coversWholeJourney()
        );
    }
}