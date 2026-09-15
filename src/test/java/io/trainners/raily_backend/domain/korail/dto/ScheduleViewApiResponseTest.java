package io.trainners.raily_backend.domain.korail.dto;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleViewApiResponseTest {

    @Test
    void 코레일_응답_JSON을_3단_구조로_파싱한다() throws Exception {
        String json = """
                {
                  "strResult": "SUCC",
                  "trn_infos": {
                    "trn_info": [
                      {
                        "h_trn_no": "1122",
                        "h_run_dt": "20260912",
                        "h_dpt_dt": "20260912",
                        "h_dpt_rs_stn_cd": "0001",
                        "h_arv_rs_stn_cd": "0007",
                        "h_dpt_stn_run_ordr": "000003",
                        "h_arv_stn_run_ordr": "000007",
                        "h_trn_clsf_cd": "04",
                        "h_trn_clsf_nm": "ITX-새마을",
                        "h_trn_gp_cd": "300",
                        "h_expct_dlay_hr": "000000"
                      }
                    ]
                  }
                }
                """;

        ObjectMapper objectMapper = new ObjectMapper();

        ScheduleViewApiResponse response = objectMapper.readValue(json, ScheduleViewApiResponse.class);

        assertEquals("SUCC", response.getStrResult());

        List<ScheduleViewResponse> trainInfoList = response.getTrainInfos().getScheduleInfoList();
        assertEquals(1, trainInfoList.size());

        ScheduleViewResponse firstTrain = trainInfoList.get(0);
        assertEquals("1122", firstTrain.getTrainNum());
        assertEquals("ITX-새마을", firstTrain.getTrainTypeName());
        assertEquals("000003", firstTrain.getDepartureStationRunOrder());
    }
}
