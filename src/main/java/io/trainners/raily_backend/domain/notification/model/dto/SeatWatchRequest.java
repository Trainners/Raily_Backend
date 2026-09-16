package io.trainners.raily_backend.domain.notification.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatWatchRequest {

    @NotBlank
    private final String trainNumber;

    @NotBlank
    private final String carNumber;

    @NotBlank
    private final String seatNumber;
}
