package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Items (
        @JsonProperty("item")List<TrainRunInfo> trainRunInfoList
){
}
