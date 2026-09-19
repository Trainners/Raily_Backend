package io.trainners.raily_backend.domain.notification.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// 프론트가 좌석 감시를 등록할 때 보내는 정보
public record SeatWatchRequest(
        @NotBlank(message = "열차 번호는 필수입니다.")
        String trainNumber,

        @NotBlank(message = "호차 번호는 필수입니다.")
        String carNumber,

        @NotBlank(message = "좌석 번호는 필수입니다.")
        String seatNumber,

        @NotBlank(message = "운행일은 필수입니다.")
        @Pattern(regexp = "\\d{8}", message = "운행일은 yyyyMMdd 형식의 8자리 숫자여야 합니다.")
        String runDate,

        @NotBlank(message = "착석역은 필수입니다.")
        String fromStation,

        @NotBlank(message = "하차역은 필수입니다.")
        String toStation,

        @NotBlank(message = "출발 시각은 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "출발 시각은 HHmmss 형식의 6자리 숫자여야 합니다.")
        String departureTime,

        @NotBlank(message = "도착 시각은 필수입니다.")
        @Pattern(regexp = "\\d{6}", message = "도착 시각은 HHmmss 형식의 6자리 숫자여야 합니다.")
        String arrivalTime
) {
}
