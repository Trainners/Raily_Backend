package io.trainners.raily_backend.domain.trainRunPlan.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/* JSON 구조
{
  "response": {
    "header": { ... },
    "body": { ... }
  }
}
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public record TrainRunInfoApiResponse (
        @JsonProperty("response")
        Response response
) {
}
