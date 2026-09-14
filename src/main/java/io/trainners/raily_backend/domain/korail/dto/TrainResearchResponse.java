package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

// 2번 응답(h_srcar_no, h_rest_seat_cnt 등)을 그대로 받는 그릇
/* JSON 형태
{
  "strResult": "SUCC",
  "srcar_infos": {
    "srcar_info": [
      { "h_srcar_no": "0003", "h_rest_seat_cnt": "00007", "h_psrm_cl_cd": "1" },
      ...
    ]
  }
}
*/

@Getter // 나중에 KorailSeatService에서 꺼내서 씀
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainResearchResponse {
    @JsonProperty("h_srcar_no")
    private String carNumber; // 호차 번호

    @JsonProperty("h_rest_seat_cnt")
    private String availableSeatCount; // 잔여석 수

    @JsonProperty("h_psrm_cl_cd")
    private String carType; // 1 = 일반실, 2 = 특실 << int로 받아도 되긴 하지만 나머지랑 통일시킴
}
