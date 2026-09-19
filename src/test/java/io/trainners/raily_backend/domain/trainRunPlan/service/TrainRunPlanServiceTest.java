package io.trainners.raily_backend.domain.trainRunPlan.service;

import io.trainners.raily_backend.domain.trainRunPlan.client.TrainRunPlanClient;
import io.trainners.raily_backend.domain.trainRunPlan.dto.Body;
import io.trainners.raily_backend.domain.trainRunPlan.dto.Items;
import io.trainners.raily_backend.domain.trainRunPlan.dto.Response;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfo;
import io.trainners.raily_backend.domain.trainRunPlan.dto.TrainRunInfoApiResponse;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainRunPlanServiceTest {

    // 무궁화 1201호 (용산 -> 익산) 실제 정차역 17개
    private static final List<String> STATIONS_1201 = List.of(
            "용산", "영등포", "수원", "평택", "천안", "아산", "온양온천", "예산", "삽교",
            "홍성", "광천", "대천", "웅천", "서천", "장항", "군산", "익산"
    );

    private final TrainRunPlanService trainRunPlanService = new TrainRunPlanService();
    private TrainRunPlanClient trainRunPlanClient;

    @BeforeEach
    void setUp() {
        trainRunPlanClient = mock(TrainRunPlanClient.class);
        when(trainRunPlanClient.fetchTrainRunInfo(anyString(), anyString()))
                .thenReturn(apiResponse(STATIONS_1201));
    }

    @Test
    void 정차역이_10개를_넘어도_11번째_이후_역까지_구간을_잘라낸다() {
        List<String> stops = trainRunPlanService.getStopsBetween(
                "20260915", "1201", "수원", "군산", trainRunPlanClient);

        assertEquals(List.of("수원", "평택", "천안", "아산", "온양온천", "예산", "삽교",
                "홍성", "광천", "대천", "웅천", "서천", "장항", "군산"), stops);
    }

    @Test
    void 정차역을_모두_받아오면_시발역부터_종착역까지_조회된다() {
        List<String> stops = trainRunPlanService.getStopsBetween(
                "20260915", "1201", "용산", "익산", trainRunPlanClient);

        assertEquals(STATIONS_1201, stops);
    }

    @Test
    void 운행_구간에_없는_역이면_STATION_NOT_ON_ROUTE_예외() {
        BusinessException e = assertThrows(BusinessException.class, () ->
                trainRunPlanService.getStopsBetween("20260915", "1201", "수원", "부산", trainRunPlanClient));

        assertEquals(ErrorCode.STATION_NOT_ON_ROUTE, e.getErrorCode());
    }

    @Test
    void 출발역이_도착역보다_뒤면_INVALID_STATION_ORDER_예외() {
        BusinessException e = assertThrows(BusinessException.class, () ->
                trainRunPlanService.getStopsBetween("20260915", "1201", "군산", "수원", trainRunPlanClient));

        assertEquals(ErrorCode.INVALID_STATION_ORDER, e.getErrorCode());
    }

    private TrainRunInfoApiResponse apiResponse(List<String> stationNames) {
        List<TrainRunInfo> infos = new ArrayList<>();
        for (int i = 0; i < stationNames.size(); i++) {
            infos.add(new TrainRunInfo(stationNames.get(i), String.valueOf(i + 1),
                    "여객승하차", null, null, "01201"));
        }
        Body body = new Body(new Items(infos), "100", "1", String.valueOf(infos.size()));
        return new TrainRunInfoApiResponse(new Response(null, body));
    }
}
