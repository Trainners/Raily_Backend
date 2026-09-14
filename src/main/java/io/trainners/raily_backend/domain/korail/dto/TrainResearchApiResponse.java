package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter // 나중에 KorailSeatService에서 꺼내서 씀
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainResearchApiResponse {
    @JsonProperty("strResult")
    private String strResult;

    @JsonProperty("srcar_infos")
    private CarInfoContainer carInfos;
}
