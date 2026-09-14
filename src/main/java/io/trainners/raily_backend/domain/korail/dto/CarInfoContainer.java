package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter // 나중에 KorailSeatService에서 꺼내서 씀
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarInfoContainer {
    @JsonProperty("srcar_info")
    private List<TrainResearchResponse> trainInfoList;
}
