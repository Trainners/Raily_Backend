package io.trainners.raily_backend.domain.notification.model.entity;

import io.trainners.raily_backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Entity
@Table(name = "seat_watches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatWatch {
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_seatwatch_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User user;

    @Column(nullable = false)
    private String trainNumber;

    @Column(nullable = false)
    private String carNumber;

    @Column(nullable = false)
    private String seatNumber;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 운행일 yyyyMMdd (코레일 h_run_dt 와 같은 형식) */
    @Column(nullable = false, length = 8)
    private String runDate;

    /** 착석한 역(감시 시작 구간의 출발역) */
    @Column(nullable = false)
    private String fromStation;

    /** 사용자의 하차역(감시 마지막 구간의 도착역) */
    @Column(nullable = false)
    private String toStation;

    /** fromStation 출발 시각 HHmmss */
    @Column(nullable = false, length = 6)
    private String departureTime;

    /** toStation 도착 시각 HHmmss */
    @Column(nullable = false, length = 6)
    private String arrivalTime;

    /** fromStation ~ toStation 정차역을 콤마로 이어 저장 */
    @Column(nullable = false, length = 1000)
    private String stops;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatWatchStatus status;

    /** 판매가 감지된 첫 구간의 출발역 */
    private String soldFromStation;

    private LocalDateTime notifiedAt;

    @Builder
    private SeatWatch(User user, String trainNumber, String carNumber, String seatNumber,
                      String runDate, String fromStation, String toStation,
                      String departureTime, String arrivalTime, List<String> stops){
        this.user = user;
        this.trainNumber = trainNumber;
        this.carNumber = carNumber;
        this.seatNumber = seatNumber;
        this.runDate = runDate;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.stops = String.join(",", stops);
        this.status = SeatWatchStatus.ACTIVE;
    }

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public List<String> getStopList() {
        return List.of(stops.split(","));
    }

    public boolean isActive() {
        return status == SeatWatchStatus.ACTIVE;
    }

    // 판매 감지 처리. 상태를 바꿔 다음 스케줄부터 감시 대상에서 빠지게 한다.
    public void markNotified(String soldFromStation) {
        this.status = SeatWatchStatus.NOTIFIED;
        this.soldFromStation = soldFromStation;
        this.notifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = SeatWatchStatus.EXPIRED;
    }

    public void cancel() {
        this.status = SeatWatchStatus.CANCELED;
    }

    // 여정이 끝났는지 판단
    public boolean isJourneyOver(LocalDateTime now) {
        LocalDate date = LocalDate.parse(runDate, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime dep = LocalTime.parse(departureTime, HHMMSS);
        LocalTime arr = LocalTime.parse(arrivalTime, HHMMSS);
        LocalDateTime arrivalAt = LocalDateTime.of(date, arr); // 실제 도착 일시
        if (arr.isBefore(dep)) { // 자정을 넘기는 열차의 경우 도착일 +1
            arrivalAt = arrivalAt.plusDays(1);
        }
        return now.isAfter(arrivalAt); // 지금이 실제 도착 일시를 지났으면 true
    }
}
