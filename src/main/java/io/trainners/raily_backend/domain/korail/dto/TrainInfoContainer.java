package io.trainners.raily_backend.domain.korail.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter // test용
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainInfoContainer {
    @JsonProperty("trn_info")
    private List<ScheduleViewResponse> scheduleInfoList;
}

//{
//  "strResult": "SUCC",                  ← ScheduleViewApiResponse
//  "trn_infos": {                        ← ScheduleViewApiResponse
//        "trn_info": [ {...}, {...} ]    ← TrainInfoContainer : [scheduleViewResponse]
//  }
//}
