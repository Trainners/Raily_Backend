package io.trainners.raily_backend.domain.korail.client;

import io.trainners.raily_backend.domain.korail.dto.ScheduleViewApiResponse;
import org.springframework.web.client.RestClient;

// 코레일 서버에 실제로 HTTP 요청을 보내는 곳.
// ScheduleView, TrainResearch, ResidualSeatsResearch.do 세 개를 호출하는 메서드가 여기 들어감
public class KorailClient {
    public ScheduleViewApiResponse fetchSchedule(
            String departureStation, String arrivalStation, String departureDate, String departureTime
    ) {
        // fetchSchedule에 요청 보낼 도구 준비
        RestClient restClient = RestClient
                .builder()
                .baseUrl("https://smart.letskorail.com:443") // 기본 주소
                .build();

        return restClient
                .post() // POST 방식
                .uri(uriBuilder -> uriBuilder
                        .path("/classes/com.korail.mobile.seatMovie.ScheduleView")
                        .queryParam("txtGoStart", departureStation)
                        .queryParam("txtGoEnd", arrivalStation)
                        .queryParam("txtGoAbrdDt", departureDate)
                        .queryParam("txtGoHour", departureTime)
                        .queryParam("radJobId", "1")
                        .queryParam("selGoTrain", "109")
                        .queryParam("txtTrnGpCd", "109")
                        .queryParam("txtPsgFlg_1", "1")
                        .queryParam("txtPsgFlg_2", "0")
                        .queryParam("txtPsgFlg_3", "0")
                        .queryParam("txtPsgFlg_5", "0")
                        .queryParam("txtPsgFlg_8", "0")
                        .queryParam("txtSeatAttCd_2", "000")
                        .queryParam("txtSeatAttCd_3", "000")
                        .queryParam("txtSeatAttCd_4", "015")
                        .queryParam("txtMenuId", "11")
                        .queryParam("txtGdNo", "")
                        .queryParam("txtJobDv", "")
                        .queryParam("txtCardPsgCnt", "0")
                        .queryParam("adjStnScdlOfrFlg", "N")
                        .queryParam("ebizCrossCheck", "N")
                        .queryParam("rtYn", "N")
                        .queryParam("srtCheckYn", "N")
                        .queryParam("Device", "AD")
                        .queryParam("Version", "250601002")
                        .build()) // korail 주소로 보냄. URL 뒤에 붙는 파라미터
                // 헤더 추가 (코레일 앱인 척하기 위한 고정 문자열)
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 13; SM-S928N Build/UP1A.231005.007)")
                .retrieve() // 요청 보내고 응답 받을 준비
                .body(ScheduleViewApiResponse.class); // 반환 타입
    }
}
