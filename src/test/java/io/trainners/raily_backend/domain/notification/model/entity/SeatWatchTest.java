package io.trainners.raily_backend.domain.notification.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeatWatchTest {

    private static final String RUN_DATE = "20260929";

    // ---------- 윈도우 판정 ----------

    @Test
    void 윈도우가_하나도_열려있지_않으면_minus1을_반환한다() {
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 4, 59))).isEqualTo(-1);
    }

    @Test
    void 도착_10분_전_정각은_윈도우에_포함된다() {
        // 평택 도착 07:15 -> 윈도우 시작 07:05
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 5, 0))).isEqualTo(1);
    }

    @Test
    void 윈도우_내부_시각은_해당_역의_인덱스를_반환한다() {
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 10, 0))).isEqualTo(1);
    }

    @Test
    void 출발_시각_정각은_윈도우에_포함된다() {
        // 평택 출발 07:16
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 16, 0))).isEqualTo(1);
    }

    @Test
    void 출발_시각을_1초라도_지나면_윈도우에서_벗어난다() {
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 16, 1))).isEqualTo(-1);
    }

    @Test
    void 다음_정차역의_윈도우가_열리면_그_인덱스를_반환한다() {
        // 수원 도착 07:30 -> 윈도우 07:20 ~ 07:32
        assertThat(기본_감시().findStopIndexInWindow(당일(7, 25, 0))).isEqualTo(2);
    }

    @Test
    void 착석역은_윈도우가_열려도_검사_대상이_아니다() {
        SeatWatch watch = 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", "070000", "070100"),   // 윈도우 06:50 ~ 07:01
                new StopSchedule("평택", "071500", "071600"),
                new StopSchedule("영등포", "080300", null)
        ));

        assertThat(watch.findStopIndexInWindow(당일(6, 55, 0))).isEqualTo(-1);
    }

    @Test
    void 하차역은_윈도우가_열려도_검사_대상이_아니다() {
        SeatWatch watch = 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", null, "070100"),
                new StopSchedule("평택", "071500", "071600"),
                new StopSchedule("영등포", "080300", "080500")  // 윈도우 07:53 ~ 08:05
        ));

        assertThat(watch.findStopIndexInWindow(당일(7, 55, 0))).isEqualTo(-1);
    }

    @Test
    void 시각_정보가_없는_중간역은_건너뛰고_다음_역을_확인한다() {
        SeatWatch watch = 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", null, "070100"),
                new StopSchedule("평택", null, null),           // 시각 없음
                new StopSchedule("수원", "073000", "073200"),
                new StopSchedule("영등포", "080300", null)
        ));

        assertThat(watch.findStopIndexInWindow(당일(7, 25, 0))).isEqualTo(2);
    }

    @Test
    void 중간_정차역이_없으면_항상_minus1을_반환한다() {
        SeatWatch watch = 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", null, "070100"),
                new StopSchedule("영등포", "080300", null)
        ));

        assertThat(watch.findStopIndexInWindow(당일(7, 30, 0))).isEqualTo(-1);
    }

    @Test
    void 두_역의_윈도우가_겹치면_앞선_역의_인덱스를_반환한다() {
        // 평택 윈도우 07:05~07:16, 수원 윈도우 07:10~07:22
        SeatWatch watch = 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", null, "070100"),
                new StopSchedule("평택", "071500", "071600"),
                new StopSchedule("수원", "072000", "072200"),
                new StopSchedule("영등포", "080300", null)
        ));

        assertThat(watch.findStopIndexInWindow(당일(7, 12, 0))).isEqualTo(1);
    }

    @Test
    void 자정을_넘기는_열차의_윈도우는_다음날로_계산한다() {
        // 23:45 출발 -> 평택 00:15 도착(다음날) -> 01:03 하차(다음날)
        SeatWatch watch = 자정_넘김_감시();

        // 평택 윈도우: 9/30 00:05 ~ 00:16
        assertThat(watch.findStopIndexInWindow(LocalDateTime.of(2026, 9, 30, 0, 10, 0))).isEqualTo(1);
        assertThat(watch.findStopIndexInWindow(LocalDateTime.of(2026, 9, 29, 23, 50, 0))).isEqualTo(-1);
    }

    // ---------- 여정 종료 판정 ----------

    @Test
    void 도착_시각_전이면_여정이_끝나지_않았다() {
        assertThat(기본_감시().isJourneyOver(당일(8, 2, 59))).isFalse();
    }

    @Test
    void 도착_시각_정각에는_아직_여정이_끝나지_않았다() {
        assertThat(기본_감시().isJourneyOver(당일(8, 3, 0))).isFalse();
    }

    @Test
    void 도착_시각을_지나면_여정이_끝났다() {
        assertThat(기본_감시().isJourneyOver(당일(8, 3, 1))).isTrue();
    }

    @Test
    void 자정을_넘기는_열차의_도착_일시는_다음날로_계산한다() {
        SeatWatch watch = 자정_넘김_감시();

        assertThat(watch.isJourneyOver(LocalDateTime.of(2026, 9, 29, 23, 50, 0))).isFalse();
        assertThat(watch.isJourneyOver(LocalDateTime.of(2026, 9, 30, 1, 10, 0))).isTrue();
    }

    // ---------- 상태 전이 ----------

    @Test
    void 생성_직후에는_ACTIVE_상태다() {
        SeatWatch watch = 기본_감시();

        assertThat(watch.getStatus()).isEqualTo(SeatWatchStatus.ACTIVE);
        assertThat(watch.isActive()).isTrue();
    }

    @Test
    void 판매가_감지되면_NOTIFIED로_전이되고_감시_대상에서_빠진다() {
        SeatWatch watch = 기본_감시();

        watch.markNotified("수원");

        assertThat(watch.getStatus()).isEqualTo(SeatWatchStatus.NOTIFIED);
        assertThat(watch.getSoldFromStation()).isEqualTo("수원");
        assertThat(watch.getNotifiedAt()).isNotNull();
        assertThat(watch.isActive()).isFalse();
    }

    // ---------- 픽스처 ----------

    // 천안(착석) 07:01 출발 -> 평택 -> 수원 -> 영등포(하차) 08:03 도착
    private SeatWatch 기본_감시() {
        return 감시(RUN_DATE, "070100", "080300", List.of(
                new StopSchedule("천안", null, "070100"),
                new StopSchedule("평택", "071500", "071600"),
                new StopSchedule("수원", "073000", "073200"),
                new StopSchedule("영등포", "080300", null)
        ));
    }

    private SeatWatch 자정_넘김_감시() {
        return 감시(RUN_DATE, "234500", "010300", List.of(
                new StopSchedule("천안", null, "234500"),
                new StopSchedule("평택", "001500", "001600"),
                new StopSchedule("영등포", "010300", null)
        ));
    }

    private SeatWatch 감시(String runDate, String departureTime, String arrivalTime, List<StopSchedule> stops) {
        return SeatWatch.builder()
                .user(null)                 // 순수 계산 로직만 검증하므로 연관은 생략
                .trainNumber("1122")
                .carNumber("0003")
                .seatNumber("7A")
                .runDate(runDate)
                .fromStation(stops.get(0).getStationName())
                .toStation(stops.get(stops.size() - 1).getStationName())
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .stops(stops)
                .build();
    }

    private LocalDateTime 당일(int hour, int minute, int second) {
        return LocalDateTime.of(2026, 9, 29, hour, minute, second);
    }
}