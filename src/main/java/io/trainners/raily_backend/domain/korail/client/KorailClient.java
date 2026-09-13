package io.trainners.raily_backend.domain.korail.client;

import io.trainners.raily_backend.domain.korail.dto.ScheduleViewApiResponse;

// 코레일 서버에 실제로 HTTP 요청을 보내는 곳.
// ScheduleView, TrainResearch, ResidualSeatsResearch.do 세 개를 호출하는 메서드가 여기 들어감
public class KorailClient {
    public ScheduleViewApiResponse fetchSchedule(
            String departureStation, String arrivalStation, String departureDate, String departureTime
    ) {
        return null;
    }
}
