package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Response (
        @JsonProperty("header")
        Header header,
        @JsonProperty("body")
        Body body
) {
}
