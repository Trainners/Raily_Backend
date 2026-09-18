package io.trainners.raily_backend.domain.korail.service;

import io.trainners.raily_backend.domain.korail.client.KorailClient;
import io.trainners.raily_backend.domain.korail.dto.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 1 → 2 → 3번을 순서대로 호출하고 결과를 조합하는 곳
public class KorailSeatService {
    /* 실행 흐름
    1. 받은 열차로 fetchTrainResearch 호출 -> 호차 목록 받음
        -> 그중 잔여석이 0보다 큰 호차만 골라냄
    2. 걸러진 호차마다 fetchAvailableSeats 호출 -> 좌석별 판매 여부를 받음
    3. 이 결과를 모아서 돌려줌
    Map<String, AvailableSeatsApiResponse> (key: 호차 번호, 값: 그 호차의 좌석 조회 결과)
    */

    public Map<String, AvailableSeatsApiResponse> showSeatLists(ScheduleViewResponse scheduleViewResponse, KorailClient korailClient) {
        TrainResearchApiResponse trainResearchApiResponse = korailClient.fetchTrainResearch(scheduleViewResponse);
        Map<String, AvailableSeatsApiResponse> result = new HashMap<>();

        // 해당 구간이 매진이면 코레일이 호차 목록 대신 strResult=FAIL("잔여석이 없습니다")을 내려줌
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
