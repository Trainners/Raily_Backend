package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Header (
        @JsonProperty("resultCode")
        String resultCode,
        @JsonProperty("resultMsg")
        String resultMsg
) {
}
