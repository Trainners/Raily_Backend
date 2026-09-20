package io.trainners.raily_backend.domain.train.controller;

import io.trainners.raily_backend.domain.korail.client.KorailClient;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewApiResponse;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewResponse;
import io.trainners.raily_backend.domain.korail.service.KorailSeatService;
import io.trainners.raily_backend.domain.seat.service.SeatRecommendationService;
import io.trainners.raily_backend.domain.train.dto.SeatMatrixResponse;
import io.trainners.raily_backend.domain.train.dto.SeatResponse;
import io.trainners.raily_backend.domain.train.dto.SegmentSeatStatus;
import io.trainners.raily_backend.domain.train.dto.TrainListResponse;
import io.trainners.raily_backend.domain.train.service.TrainService;
import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.service.TrainRunPlanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController // HTTP 요청을 받아서 데이터(JSON)로 응답하는 곳
public class TrainController {
    // TrainRunPlanClient 인스턴스를 자동으로 넣어주도록 생성자 추가
    public TrainController(TrainRunPlanClient trainRunPlanClient,
                           SeatRecommendationService seatRecommendationService) {
        this.trainRunPlanClient = trainRunPlanClient;
        this.seatRecommendationService = seatRecommendationService;
    }
    private final TrainRunPlanClient trainRunPlanClient;
    private final SeatRecommendationService seatRecommendationService;

    // 열차 리스트 조회 엔드포인트
    @GetMapping("/api/trains")
    public List<TrainListResponse> getTrainList(
            @RequestParam String departureStation,
            @RequestParam String arrivalStation,
            @RequestParam String date,
            @RequestParam String time
    ) {
        KorailClient korailClient = new KorailClient(); // 일단 의존성 주입 생략함

        ScheduleViewApiResponse response =
                korailClient.fetchScheduleView(departureStation, arrivalStation, date, time);

        // 코레일에서 열차 목록 받아옴 & null 처리
        List<ScheduleViewResponse> trains;
        if(response.getTrainInfos() == null) {
            trains = new ArrayList<>();
        } else {
            trains = response.getTrainInfos().getScheduleInfoList();
        }

        List<TrainListResponse> result = new ArrayList<>();

        // 하나씩 돌면서 ScheduleViewResponse의 값을 TrainListResponse로 옮겨담음
        for(ScheduleViewResponse train : trains) {
            TrainListResponse item = new TrainListResponse();

            item.setTrainNum(train.getTrainNum());
            item.setTrainTypeName(train.getTrainTypeName());
            item.setDepartureTime(train.getDepartureTime());
            item.setArrivalTime(train.getArrivalTime());
            item.setExpectedDelay(train.getExpectedDelay());
            result.add(item);
        }

        return result;
    }

    // 구간별 좌석 조회 엔드포인트 - 우선순위에 따라 정렬된 좌석 목록을 내려줌
    @GetMapping("/api/trains/seats")
    public SeatMatrixResponse getRecommendedSeats(
            @RequestParam String departureStation,
            @RequestParam String arrivalStation,
            @RequestParam String date,
            @RequestParam String time,
            @RequestParam String trainNum
    ) {
        SegmentSeatStatus segmentSeatStatus =
                fetchSegmentSeatStatus(departureStation, arrivalStation, date, time, trainNum);

        List<SeatResponse> seats = seatRecommendationService.recommend(segmentSeatStatus.segments())
                .stream()
                .map(SeatResponse::from)
                .toList();

        return new SeatMatrixResponse(segmentSeatStatus.stops(), seats);
    }

    // 코레일에서 구간별 좌석 원본을 수집 (열차 매칭 -> 구간별 좌석 조회)
    private SegmentSeatStatus fetchSegmentSeatStatus(
            String departureStation, String arrivalStation, String date, String time, String trainNum
    ) {
        KorailClient korailClient = new KorailClient();
        TrainRunPlanService trainRunPlanService = new TrainRunPlanService();

        ScheduleViewApiResponse response =
                korailClient.fetchScheduleView(departureStation, arrivalStation, date, time);

        // 코레일에서 열차 목록 받아옴
        List<ScheduleViewResponse> candidates =
                response.getTrainInfos().getScheduleInfoList();

        ScheduleViewResponse matchedTrain = null;
        for(ScheduleViewResponse candidate : candidates) {
            if(Integer.parseInt(candidate.getTrainNum()) == Integer.parseInt(trainNum)) {
                matchedTrain = candidate;
                break;
            }
        }

        if (matchedTrain == null) {
            throw new IllegalArgumentException("선택한 열차를 찾을 수 없습니다.");
        }

        TrainService trainService = new TrainService();
        KorailSeatService korailSeatService = new KorailSeatService();

        return trainService.seatStatus(departureStation, arrivalStation, matchedTrain,
                korailClient, korailSeatService, trainRunPlanService, trainRunPlanClient);
    }
}