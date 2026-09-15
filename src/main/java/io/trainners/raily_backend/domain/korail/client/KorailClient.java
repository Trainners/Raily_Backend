package io.trainners.raily_backend.domain.korail.client;

import io.trainners.raily_backend.domain.korail.dto.*;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.CookieManager;
import java.net.http.HttpClient;
import java.util.List;

// 코레일 서버에 실제로 HTTP 요청을 보내는 곳.
// ScheduleView, TrainResearch, AvailableSeats.do 세 개를 호출하는 메서드가 여기 들어감
public class KorailClient {


    private static final String deviceId = "558a4f02041657ea";

    // 토큰을 생성하는 객체 선언
    private final DynaPathEngine engine = new DynaPathEngine();

    // 코레일은 응답 본문이 JSON이어도 Content-Type을 text/html로 잘못 내려줌.
    // 기본 Jackson 컨버터는 application/json만 처리하므로, text/html도 JSON으로 파싱하도록 직접 등록.
    private final RestClient restClient = RestClient
            .builder()
            .baseUrl("https://smart.letskorail.com:443") // 기본 주소
            .requestFactory(new JdkClientHttpRequestFactory(     // ← 새로 추가
                    HttpClient.newBuilder()
                            .cookieHandler(new CookieManager())
                            .build()
            ))
            .messageConverters(converters -> {
                MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
                jsonConverter.setSupportedMediaTypes(List.of(
                        MediaType.APPLICATION_JSON,
                        MediaType.TEXT_HTML,
                        MediaType.TEXT_PLAIN
                ));
                converters.add(0, jsonConverter);
            })
            .build();

    public ScheduleViewApiResponse fetchScheduleView(
            String departureStation, String arrivalStation, String departureDate, String departureTime
    ) {
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
                .header("x-dynapath-m-token", engine.generateToken(deviceId, System.currentTimeMillis(), DynaPathEngine.randomRand()))
                .retrieve() // 요청 보내고 응답 받을 준비
                .body(ScheduleViewApiResponse.class); // 반환 타입
    }

    public TrainResearchApiResponse fetchTrainResearch(ScheduleViewResponse train) {
        // form 형식 데이터를 보낼 땐 MultiValueMap 사용 (키 하나에 값을 여러 개 담을 수 있는 Map)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        // 동적 값
        formData.add("txtTrnNo", train.getTrainNum()); // ("키 이름", "값")
        formData.add("txtRunDt", train.getRunDate());
        formData.add("txtDptDt", train.getDepartureDate());
        formData.add("txtDptRsStnCd", train.getDepartureStationCode());
        formData.add("txtArvRsStnCd", train.getArrivalStationCode());
        formData.add("txtDptStnRunOrdr", train.getDepartureStationRunOrder());
        formData.add("txtArvStnRunOrdr", train.getArrivalStationRunOrder());
        formData.add("txtTrnClsfCd", train.getTrainTypeCode());
        formData.add("txtTrnGpCd", train.getTrainGroupCode());
        // 고정값
        formData.add("txtPsrmClCd", "1");
        formData.add("txtSeatAttCd", "015");
        formData.add("txtTotPsgCnt", "1");
        formData.add("txtMenuId", "11");
        formData.add("txtGdNo", "");
        formData.add("Device", "AD");
        formData.add("Version", "250601002");

        return restClient
                .post()
                .uri("/classes/com.korail.mobile.research.TrainResearch")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 13; SM-S928N Build/UP1A.231005.007)")
                .header("x-dynapath-m-token", engine.generateToken(deviceId, System.currentTimeMillis(), DynaPathEngine.randomRand()))
                .body(formData) // 요청 본문에 데이터 실어 보냄
                .retrieve()
                .body(TrainResearchApiResponse.class); // 응답은 이 타입으로 바꿔서 받음
    }

    public AvailableSeatsApiResponse fetchAvailableSeats(ScheduleViewResponse train, TrainResearchResponse car) {
        // form 형식 데이터를 보낼 땐 MultiValueMap 사용 (키 하나에 값을 여러 개 담을 수 있는 Map)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

        // 동적 값
        formData.add("txtSrcarNo", car.getCarNumber());
        formData.add("txtTrnNo", train.getTrainNum()); // ("키 이름", "값")
        formData.add("txtRunDt", train.getRunDate());
        formData.add("txtDptDt", train.getDepartureDate());
        formData.add("txtDptRsStnCd", train.getDepartureStationCode());
        formData.add("txtArvRsStnCd", train.getArrivalStationCode());
        formData.add("txtDptStnRunOrdr", train.getDepartureStationRunOrder());
        formData.add("txtArvStnRunOrdr", train.getArrivalStationRunOrder());
        formData.add("txtTrnClsfCd", train.getTrainTypeCode());
        formData.add("txtTrnGpCd", train.getTrainGroupCode());
        // 고정값
        formData.add("txtPsrmClCd", "1");
        formData.add("txtSeatAttCd", "015");
        formData.add("txtTotPsgCnt", "1");
        formData.add("txtMenuId", "11");
        formData.add("txtGdNo", "");
        formData.add("Device", "AD");
        formData.add("Version", "250601002");

        return restClient
                .post()
                .uri("/classes/com.korail.mobile.research.ResidualSeatsResearch.do")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 13; SM-S928N Build/UP1A.231005.007)")
                .header("x-dynapath-m-token", engine.generateToken(deviceId, System.currentTimeMillis(), DynaPathEngine.randomRand()))
                .body(formData) // 요청 본문에 데이터 실어 보냄
                .retrieve()
                .body(AvailableSeatsApiResponse.class); // 응답은 이 타입으로 바꿔서 받음
    }
}
