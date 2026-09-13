package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.trainners.raily_backend.domain.korail.container.TrainInfoContainer;
import lombok.Getter;

@Getter // test용
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleViewApiResponse {
    // strResult 담을 String 필드
    @JsonProperty("strResult")
    private String strResult;

    // trn_infos 담을 TrainInfoContainer타입 필드
    @JsonProperty("trn_infos")
    private TrainInfoContainer trnInfos;
}
