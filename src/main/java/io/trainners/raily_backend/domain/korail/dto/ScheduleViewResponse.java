package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 1번 응답(h_trn_no, h_dpt_stn_run_ordr 등)을 그대로 받는 그릇
/* JSON 구조
{
  "strResult": "SUCC",
  "trn_infos": {
    "trn_info": [
      { "h_trn_no": "1122", ... },   ← ScheduleViewResponse #1
      { "h_trn_no": "1234", ... }    ← ScheduleViewResponse #2
    ]
  }
}
*/
@Getter // test용
@JsonIgnoreProperties(ignoreUnknown = true) // 현재 정의돼있는 필드보다 더 많은 필드가 응답으로 옴
public class ScheduleViewResponse {
    @JsonProperty("h_trn_no")
    private String trainNum; // 열차 번호

    @JsonProperty("h_run_dt")
    private String runDate; // 운행일

    @JsonProperty("h_dpt_dt")
    private String departureDate; // 출발일

    @JsonProperty("h_dpt_rs_stn_cd")
    private String departureStationCode; // 출발역 코드

    @JsonProperty("h_arv_rs_stn_cd")
    private String arrivalStationCode; // 도착역 코드

    @JsonProperty("h_dpt_stn_run_ordr")
    private String departureStationRunOrder; // 운행 순번(출발)

    @JsonProperty("h_arv_stn_run_ordr")
    private String arrivalStationRunOrder; // 운행 순번(도착)

    @JsonProperty("h_trn_clsf_cd")
    private String trainTypeCode; // 열차 종류별 코드

    @JsonProperty("h_trn_clsf_nm")
    private String trainTypeName; // 열차명

    @JsonProperty("h_trn_gp_cd")
    private String trainGroupCode; // 열차 그룹별 코드

    @JsonProperty("h_expct_dlay_hr")
    private String expectedDelay; // 예상 지연
}
