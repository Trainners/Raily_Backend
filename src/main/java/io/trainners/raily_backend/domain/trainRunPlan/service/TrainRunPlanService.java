package io.trainners.raily_backend.domain.trainRunPlan.service;

import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfo;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfoApiResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrainRunPlanService {
    // 응답에서 리스트 꺼내기
    public List<String> getStopStations(String runDate, String trainNo, TrainRunPlanClient trainRunPlanClient) {
        TrainRunInfoApiResponse trainRunInfoApiResponse = trainRunPlanClient.fetchTrainRunInfo(lastWeekRunDate(runDate), padTrainNo(trainNo));
        List<TrainRunInfo> trainRunInfoList =
                trainRunInfoApiResponse.response()
                        .body()
                        .items()
                        .trainRunInfoList();

        // 역 이름만 담긴 리스트로 변환해 반환
        List<String> result = new ArrayList<>();

        for(TrainRunInfo trainRunInfo : trainRunInfoList) {
            result.add(trainRunInfo.stationName());
        }

        return result;
    }

    // 지난주 같은 요일의 결과를 보기 위한 날짜 계산 로직 (헬퍼)
    private String lastWeekRunDate(String targetRunDate) {
        // 문자열 날짜를 LocalDate로 파싱
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate date = LocalDate.parse(targetRunDate, dateTimeFormatter);

        // 조회 가능한 마지막 날짜 == 어제이므로 어제 날짜 구하기
        LocalDate yesterday = LocalDate.now().minusDays(1);
        //while(date > yesterday) { // LocalDate는 객체이므로 비교연산자 사용 불가
        while(date.isAfter(yesterday)) {
            date = date.minusDays(7);
        }

        // 조건을 만족하는 Localdate date 문자열로 바꿔서 반환
        return date.format(dateTimeFormatter);
    }

    // 0 채움 처리 헬퍼
    private String padTrainNo(String trainNo) {
        // 5자리 0채움 문자열 반환
        return String.format("%05d", Integer.parseInt(trainNo));
    }

    // 기존 하드코딩된 데이터에 대해 정차 구간을 구하던 메서드 변경
    public List<String> getStopsBetween(String runDate, String trainNo, String dptStn, String arrStn, TrainRunPlanClient trainRunPlanClient) {
        List<String> stops = getStopStations(runDate, trainNo, trainRunPlanClient);
        int dptStnIdx = stops.indexOf(dptStn);
        int arrStnIdx = stops.indexOf(arrStn);

        return stops.subList(dptStnIdx, arrStnIdx + 1);
    }

    // 역별 도착시각까지 가져오는 메서드(스케쥴러용)
    public List<TrainRunInfo> getTrainRunInfoList(String runDate, String trainNo, TrainRunPlanClient trainRunPlanClient){
        TrainRunInfoApiResponse trainRunInfoApiResponse = trainRunPlanClient.fetchTrainRunInfo(lastWeekRunDate(runDate), trainNo);
        List<TrainRunInfo> trainRunInfoList =
                trainRunInfoApiResponse.response()
                        .body()
                        .items()
                        .trainRunInfoList();

        return trainRunInfoList;
    }
}
