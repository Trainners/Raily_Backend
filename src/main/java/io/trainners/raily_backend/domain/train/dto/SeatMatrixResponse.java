package io.trainners.raily_backend.domain.train.dto;

import java.util.List;

// 프론트에 보내줄 좌석 조회 응답
public record SeatMatrixResponse(
        List<String> stops,
        List<SeatResponse> seats
) {}