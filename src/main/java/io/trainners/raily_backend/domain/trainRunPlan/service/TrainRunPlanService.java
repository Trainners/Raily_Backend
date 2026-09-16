package io.trainners.raily_backend.domain.trainRunPlan.service;

import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfo;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfoApiResponse;

import java.util.ArrayList;
import java.util.List;

public class TrainRunPlanService {
    // 응답에서 리스트 꺼내기
    public List<String> getStopStations(String runDate, String trainNo, TrainRunPlanClient trainRunPlanClient) {
        TrainRunInfoApiResponse trainRunInfoApiResponse = trainRunPlanClient.fetchTrainRunInfo(runDate, trainNo);
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

    // 지난주 같은 요일의 결과를 보기 위한 날짜 계산 로직
//    private String lastWeekRunDate
}
