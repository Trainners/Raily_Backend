package io.trainners.raily_backend.domain.trainRunPlan.client;

import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfoApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

public class TrainRunPlanClient {
    // API 일반 인증키 (키 값은 application.yml)
    @Value("${trainrunplan.service-key}") private String serviceKey;

    // API 호출
    public TrainRunInfoApiResponse fetchTrainRunInfo(String runDate, String trainNo) {

        return RestClient.builder()
                .baseUrl("https://apis.data.go.kr/B551457/run/v2")
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder.path("/travelerTrainRunInfo2")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("returnType", "JSON")
                        .queryParam("cond[run_ymd::EQ]", runDate)
                        .queryParam("cond[trn_no::EQ]", trainNo)
                        .build())
                .retrieve()
                .body(TrainRunInfoApiResponse.class);

    }
}
