package io.trainners.raily_backend.domain.train.service;

import io.trainners.raily_backend.domain.korail.client.KorailClient;
import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsApiResponse;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewApiResponse;
import io.trainners.raily_backend.domain.korail.dto.ScheduleViewResponse;
import io.trainners.raily_backend.domain.korail.service.KorailSeatService;
import io.trainners.raily_backend.domain.train.dto.SegmentSeatStatus;
import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.service.TrainRunPlanService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TrainService {
    // 각 구간마다 fetchScheduleView -> KorailSeatService.showSeatLists
    public SegmentSeatStatus seatStatus(
            String departureStation, String arrivalStation, ScheduleViewResponse scheduleViewResponse,
            KorailClient korailClient, KorailSeatService korailSeatService,
            TrainRunPlanService trainRunPlanService, TrainRunPlanClient trainRunPlanClient
    ) {
        String currentTime = scheduleViewResponse.getDepartureTime(); // 처음에 선택한 열차의 출발시각
        String targetTrainNum = scheduleViewResponse.getTrainNum();   // 추적할 열차번호
        String date = scheduleViewResponse.getRunDate();              // 운행일

        List<String> stops = trainRunPlanService.getStopsBetween(date, targetTrainNum,
                departureStation, arrivalStation, trainRunPlanClient);
        Map<String, Map<String, AvailableSeatsApiResponse>> result = new LinkedHashMap<>();

        for (int i = 0; i < stops.size() - 1; i++) {
            String segDep = stops.get(i);       // 이번 구간 출발역
            String segArr = stops.get(i + 1);   // 이번 구간 도착역
            ScheduleViewApiResponse scheduleResponse = korailClient.fetchScheduleView(segDep, segArr, date, currentTime);
            List<ScheduleViewResponse> candidates = scheduleResponse.getTrainInfos().getScheduleInfoList();

            ScheduleViewResponse matchedTrain = null;
            for (ScheduleViewResponse candidate : candidates) {
                if (Integer.parseInt(candidate.getTrainNum()) == Integer.parseInt(targetTrainNum)) {
                    matchedTrain = candidate;
                    break;
                }
            }

            if (matchedTrain == null) {
                result.put(segDep + "-" + segArr, Map.of());
                continue;
            }

            Map<String, AvailableSeatsApiResponse> segmentSeats = korailSeatService.showSeatLists(matchedTrain, korailClient);
            result.put(segDep + "-" + segArr, segmentSeats);

            currentTime = matchedTrain.getArrivalTime();
        }
        return new SegmentSeatStatus(stops, result);
    }
}
