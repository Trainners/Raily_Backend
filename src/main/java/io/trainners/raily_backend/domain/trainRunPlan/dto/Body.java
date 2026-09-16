package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Body(
        @JsonProperty("items")
        Items items,
        @JsonProperty("numOfRows")
        String numOfRows,
        @JsonProperty("pageNo")
        String pageNo,
        @JsonProperty("totalCount")
        String totalCount
) {






}
