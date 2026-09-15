package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableSeatsApiResponse {
    @JsonProperty("strResult")
    private String strResult;

    @JsonProperty("seat_infos")
    private SeatInfoContainer seatInfos;
}
