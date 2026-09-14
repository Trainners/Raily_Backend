package io.trainners.raily_backend.domain.train.dto;

import lombok.Getter;
import lombok.Setter;

// 프론트에 내려줄 "열차 리스트" 모양. 추후 수정 필요
@Setter
@Getter
public class TrainListResponse {
    private String trainNum; // 열차 번호
    private String trainTypeName; // 열차명
    private String departureTime; // 출발 시각
    private String arrivalTime; // 도착 시각
    private String expectedDelay; // 예상 지연
}
