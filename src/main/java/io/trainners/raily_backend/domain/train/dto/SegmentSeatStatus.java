package io.trainners.raily_backend.domain.train.dto;

import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsApiResponse;

import java.util.List;
import java.util.Map;

public record SegmentSeatStatus(
        // 정차역 목록
        List<String> stops,
        // 구간별 좌석 원본
        Map<String, Map<String, AvailableSeatsApiResponse>> segments
) {}