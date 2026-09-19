package io.trainners.raily_backend.domain.notification.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // 독립된 테이블·id를 갖지 않고, 다른 엔티티에 끼워 넣어지는 값 묶음
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StopSchedule {

    @Column(nullable = false)
    private String stationName;

    /** 이 역 도착 시각 HHmmss. 시발역은 null */
    @Column(length = 6)
    private String arrivalTime;

    /** 이 역 출발 시각 HHmmss. 종착역은 null */
    @Column(length = 6)
    private String departureTime;

    public StopSchedule(String stationName, String arrivalTime, String departureTime) {
        this.stationName = stationName;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }
}