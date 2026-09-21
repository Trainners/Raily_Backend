package io.trainners.raily_backend.domain.trainRunPlan.service;

import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfo;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfoApiResponse;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service // 스케줄러 & SeatWatchService가 주입받을 수 있게 빈으로 등록
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

        // index == -1일 경우 처리하는 코드
        if(dptStnIdx == -1 || arrStnIdx == -1){
            throw new BusinessException(ErrorCode.STATION_NOT_ON_ROUTE);
        }
        if (dptStnIdx >= arrStnIdx) {
            throw new BusinessException(ErrorCode.INVALID_STATION_ORDER);
        }

        return stops.subList(dptStnIdx, arrStnIdx + 1);
    }

    // 착석역 ~ 하차역 구간의 정차역을 도착 / 출발 시각까지 포함해 잘라 반환함
    // 전에 생성한 getStopsBetween은 역 이름만 주기 때문에 감시 윈도우 계산에는 사용할 수 없다.
    public List<TrainRunInfo> getTrainRunInfosBetween(
            String runDate, String trainNo, String dptStn,
            String arrStn, TrainRunPlanClient trainRunPlanClient
    ) {
        List<TrainRunInfo> infos = getTrainRunInfoList(
                runDate, trainNo, trainRunPlanClient
        );

        // 공공데이터에 해당 열차 운행 기록이 없으면 이후 인덱스 계산이 전부 무의미하다
        if (infos.isEmpty()) {
            throw new BusinessException(ErrorCode.TRAIN_NOT_FOUND);
        }

        // 역 이름 리스트로 인덱스를 찾는다. indexOf 는 List<TrainRunInfo> 에 바로 쓸 수 없다.
        int dptStnIdx = -1;
        int arrStnIdx = -1;
        for (int i = 0; i < infos.size(); i++) {
            String stationName = infos.get(i).stationName();
            if (dptStnIdx == -1 && stationName.equals(dptStn)) {
                dptStnIdx = i;
            }
            if (stationName.equals(arrStn)) {
                arrStnIdx = i;   // 같은 역을 두 번 지나는 노선을 대비해 마지막 것을 쓴다
            }
        }

        // getStopsBetween 과 동일한 검증 규칙
        if (dptStnIdx == -1 || arrStnIdx == -1) {
            throw new BusinessException(ErrorCode.STATION_NOT_ON_ROUTE);
        }
        if (dptStnIdx >= arrStnIdx) {
            throw new BusinessException(ErrorCode.INVALID_STATION_ORDER);
        }

        return infos.subList(dptStnIdx, arrStnIdx + 1);
    }

    // 역별 도착시각까지 가져오는 메서드(스케쥴러용)
    public List<TrainRunInfo> getTrainRunInfoList(
            String runDate, String trainNo, TrainRunPlanClient trainRunPlanClient
    ){
        TrainRunInfoApiResponse trainRunInfoApiResponse =
                trainRunPlanClient.fetchTrainRunInfo(lastWeekRunDate(runDate),
                        padTrainNo(trainNo)); // 0 채움 처리
        List<TrainRunInfo> trainRunInfoList =
                trainRunInfoApiResponse.response()
                        .body()
                        .items()
                        .trainRunInfoList();

        return trainRunInfoList;
    }
}
