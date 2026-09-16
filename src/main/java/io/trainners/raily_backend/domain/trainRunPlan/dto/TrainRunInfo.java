package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainRunInfo(
        @JsonProperty("stn_nm") String stationName,
        @JsonProperty("trn_run_sn") String runSequence,
        @JsonProperty("stop_se_nm") String stopType,
        @JsonProperty("trn_arvl_dt") String arrivalDateTime,
        @JsonProperty("trn_dptre_dt") String departureDateTime,
        @JsonProperty("trn_no") String trainNo
) {

}
