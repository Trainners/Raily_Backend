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
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_watches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatWatch {
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");
    // 감시 윈도우 크기(분). 도착 예정 시각 기준 이만큼 전부터 조회를 시작한다. 운영하며 조정 가능
    private static final int WINDOW_MINUTES = 10;

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

    /** fromStation ~ toStation 정차역과 각 역의 도착/출발 시각 */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "seat_watch_stops",
            joinColumns = @JoinColumn(name = "seat_watch_id"),
            foreignKey = @ForeignKey(
                    name = "fk_seat_watch_stops_seat_watch",
                    foreignKeyDefinition = "FOREIGN KEY (seat_watch_id) REFERENCES seat_watches(id) ON DELETE CASCADE"
            )
    )
    @OrderColumn(name = "stop_order")
    private List<StopSchedule> stops = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatWatchStatus status;

    /** 판매가 감지된 첫 구간의 출발역 */
    private String soldFromStation;

    private LocalDateTime notifiedAt;

    @Builder
    private SeatWatch(User user, String trainNumber, String carNumber, String seatNumber,
                      String runDate, String fromStation, String toStation,
                      String departureTime, String arrivalTime, List<StopSchedule> stops){
        this.user = user;
        this.trainNumber = trainNumber;
        this.carNumber = carNumber;
        this.seatNumber = seatNumber;
        this.runDate = runDate;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.stops = new ArrayList<>(stops);
        this.status = SeatWatchStatus.ACTIVE;
    }

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
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

    /** 지금 감시 윈도우(도착 10분 전 ~ 출발)에 든 중간 정차역의 인덱스. 없으면 -1 */
    public int findStopIndexInWindow(LocalDateTime now) {
        for (int i = 1; i <= stops.size() - 2; i++) { // 0은 출발역, 마지막은 종착역이므로 제외
            StopSchedule stop = stops.get(i);

            if (stop.getArrivalTime() == null || stop.getDepartureTime() == null) {
                continue; // 시각 정보가 없는 역은 건너뛰고 다음 역 확인
            }

            LocalDateTime start = toDateTime(stop.getArrivalTime()).minusMinutes(WINDOW_MINUTES);
            LocalDateTime end = toDateTime(stop.getDepartureTime());

            if (!now.isBefore(start) && !now.isAfter(end)) { // now가 start 이상이고 end 이하이면
                return i;
            }
        }

        return -1; // 모든 정차역을 확인했지만 윈도우에 든 역이 없음
    }


    // stopIndex번째 정차역의 출발 일시. 코레일 조회에 날짜와 시각을 함께 넘겨야 해서 필요함
    // 자정을 넘기는 열차는 toDateTime 이 날짜를 +1일 보정한다
    public LocalDateTime departureDateTimeOf(int stopIndex) {
        return toDateTime(stops.get(stopIndex).getDepartureTime());
    }

    // 여정이 끝났는지 판단
    public boolean isJourneyOver(LocalDateTime now) {
        return now.isAfter(toDateTime(arrivalTime));
//        LocalDate date = LocalDate.parse(runDate, DateTimeFormatter.BASIC_ISO_DATE);
//        LocalTime dep = LocalTime.parse(departureTime, HHMMSS);
//        LocalTime arr = LocalTime.parse(arrivalTime, HHMMSS);
//        LocalDateTime arrivalAt = LocalDateTime.of(date, arr); // 실제 도착 일시
//        if (arr.isBefore(dep)) { // 자정을 넘기는 열차의 경우 도착일 +1
//            arrivalAt = arrivalAt.plusDays(1);
//        }
//        return now.isAfter(arrivalAt); // 지금이 실제 도착 일시를 지났으면 true
    }

    /** runDate + HHmmss 를 실제 일시로 변환. 출발 시각보다 이르면 자정을 넘긴 것으로 보고 +1일 */
    private LocalDateTime toDateTime(String hhmmss) {
        LocalDate date = LocalDate.parse(runDate, DateTimeFormatter.BASIC_ISO_DATE);
        LocalTime time = LocalTime.parse(hhmmss, HHMMSS);
        LocalTime depTime = LocalTime.parse(this.departureTime, HHMMSS);

        if (time.isBefore(depTime)) { // 자정 넘은 경우
            return LocalDateTime.of(date, time).plusDays(1);
        }

        return LocalDateTime.of(date, time);
    }
}
