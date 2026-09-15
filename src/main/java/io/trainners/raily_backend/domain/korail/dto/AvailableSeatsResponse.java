package io.trainners.raily_backend.domain.korail.dto;

// 3번 응답(h_con_seat_no, h_sale_psb_flg 등)을 그대로 받는 그릇

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableSeatsResponse {
    @JsonProperty("h_con_seat_no")
    private String seatNumber; // 좌석 번호

    @JsonProperty("h_sale_psb_flg")
    private String isSellable; // "Y"면 판매 가능(빈 자리), 그 외는 판매됨
}
