package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.korail.client.KorailClient;
import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsApiResponse;
import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsResponse;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewApiResponse;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewResponse;
import io.trainners.raily_backend.domain.korail.dto.TrainResearchApiResponse;
import io.trainners.raily_backend.domain.korail.dto.TrainResearchResponse;
import io.trainners.raily_backend.domain.notification.model.dto.SeatCheckResult;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.StopSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


// 감시 윈도우가 열린 정차역에 대해 그 역에서 출발하는 한 구간의 좌석이 팔렸는지 확인한다
// 확인에 실패하면(코레일 오류·응답 이상) 판매로 단정하지 않고 stillFree 를 돌려준다
// (잘못된 알림을 보내는 쪽이 한 틱 늦게 감지하는 쪽보다 나쁘기 때문)
@Slf4j
@Component
public class SeatAvailabilityChecker {
    private static final String SELLABLE = "Y";   // h_sale_psb_flg 가 "Y"면 빈 자리
    private static final DateTimeFormatter HHMMSS = DateTimeFormatter.ofPattern("HHmmss");


    // @param stopIndex SeatWatch.findStopIndexInWindow 가 돌려준 정차역 인덱스
    public SeatCheckResult check(SeatWatch watch, int stopIndex) {
        List<StopSchedule> stops = watch.getStops();
        StopSchedule from = stops.get(stopIndex);
        StopSchedule to = stops.get(stopIndex + 1);

        try {
            // 코레일 세션(쿠키)이 호출 간에 섞이지 않도록 매 검사마다 새로 만든다
            // 기존 TrainController 도 요청마다 new KorailClient() 를 쓰고 있다
            KorailClient korailClient = new KorailClient();

            LocalDateTime boarding = watch.departureDateTimeOf(stopIndex);
            String date = boarding.format(DateTimeFormatter.BASIC_ISO_DATE);
            String time = boarding.format(HHMMSS);

            // 1) 이 구간을 운행하는 열차 목록에서 내 열차를 찾는다
            ScheduleViewApiResponse schedule = korailClient.fetchScheduleView(
                    from.getStationName(), to.getStationName(), date, time);

            ScheduleViewResponse train = findTrain(schedule, watch.getTrainNumber());
            if (train == null) {
                // 매진이라 목록에서 빠졌을 수도, 시간표가 바뀌었을 수도 있다. 단정 X
                log.debug("열차를 찾지 못함 watchId={} {}->{}", watch.getId(),
                        from.getStationName(), to.getStationName());
                return SeatCheckResult.stillFree();
            }

            // 2) 내 호차의 잔여석을 본다
            TrainResearchApiResponse research = korailClient.fetchTrainResearch(train);
            if (research.getCarInfos() == null) {
                return SeatCheckResult.stillFree();
            }

            TrainResearchResponse car = findCar(research.getCarInfos().getTrainInfoList(), watch.getCarNumber());
            if (car == null || parseCount(car.getAvailableSeatCount()) == 0) {
                // 호차에 빈 자리가 하나도 없다 = 내 자리도 팔렸다
                // 좌석 목록 조회를 건너뛰어 코레일 호출을 한 번 아낌
                return SeatCheckResult.soldFrom(from.getStationName());
            }

            // 3) 내 좌석 하나의 판매 여부를 본다
            AvailableSeatsApiResponse seats = korailClient.fetchAvailableSeats(train, car);
            if (seats.getSeatInfos() == null || seats.getSeatInfos().getSeatInfoList() == null) {
                return SeatCheckResult.stillFree();
            }

            for (AvailableSeatsResponse seat : seats.getSeatInfos().getSeatInfoList()) {
                if (watch.getSeatNumber().equals(seat.getSeatNumber())) {
                    return SELLABLE.equals(seat.getIsSellable())
                            ? SeatCheckResult.stillFree()
                            : SeatCheckResult.soldFrom(from.getStationName());
                }
            }

            // 목록에 내 좌석이 아예 없다 = 판매 가능 목록에서 빠졌다는 뜻으로 본다
            return SeatCheckResult.soldFrom(from.getStationName());

        } catch (Exception e) {
            // 한 건의 실패가 스케줄러 전체를 멈추지 않도록 여기서 삼킨다
            log.warn("좌석 확인 실패 watchId={} {}->{} : {}", watch.getId(),
                    from.getStationName(), to.getStationName(), e.toString());
            return SeatCheckResult.stillFree();
        }
    }

    private ScheduleViewResponse findTrain(ScheduleViewApiResponse schedule, String trainNumber) {
        if (schedule == null || schedule.getTrainInfos() == null
                || schedule.getTrainInfos().getScheduleInfoList() == null) {
            return null;
        }
        int target = Integer.parseInt(trainNumber);
        for (ScheduleViewResponse candidate : schedule.getTrainInfos().getScheduleInfoList()) {
            // "01122" 와 "1122" 를 같게 보려면 숫자로 비교해야 한다
            if (Integer.parseInt(candidate.getTrainNum()) == target) {
                return candidate;
            }
        }
        return null;
    }

    private TrainResearchResponse findCar(List<TrainResearchResponse> cars, String carNumber) {
        if (cars == null) {
            return null;
        }
        for (TrainResearchResponse car : cars) {
            if (carNumber.equals(car.getCarNumber())) {
                return car;
            }
        }
        return null;
    }

    private int parseCount(String count) {
        try {
            return Integer.parseInt(count);
        } catch (NumberFormatException e) {
            return -1;   // 파싱 실패 시 0으로 오인해 판매 처리하지 않도록 음수를 돌려준다
        }
    }
}