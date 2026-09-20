package io.trainners.raily_backend.domain.korail.service;

import io.trainners.raily_backend.domain.korail.client.KorailClient;
import io.trainners.raily_backend.domain.korail.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KorailSeatService {

    public Map<String, AvailableSeatsApiResponse> showSeatLists(ScheduleViewResponse scheduleViewResponse, KorailClient korailClient) {
        TrainResearchApiResponse trainResearchApiResponse = korailClient.fetchTrainResearch(scheduleViewResponse);
        Map<String, AvailableSeatsApiResponse> result = new HashMap<>();

        if (trainResearchApiResponse.getCarInfos() == null) {
            return result;
        }
        List<TrainResearchResponse> carList = trainResearchApiResponse.getCarInfos().getTrainInfoList();

        for(TrainResearchResponse car : carList) {
            // carList 안의 호차 하나하나에 대해 코드 반복 실행
            int seatCount = Integer.parseInt(car.getAvailableSeatCount()); // 문자열 -> 숫자 변환
            if(seatCount > 0) {
                AvailableSeatsApiResponse availableSeats = korailClient.fetchAvailableSeats(scheduleViewResponse, car);
                result.put(car.getCarNumber(), availableSeats);
            }
        }

        return result;
    }
}
