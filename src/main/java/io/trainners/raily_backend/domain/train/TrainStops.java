package io.trainners.raily_backend.domain.train;

import java.util.List;

public class TrainStops {
    private static final List<String> stops = List.of("천안", "평택", "수원", "영등포");

    public List<String> getStopsBetween(String dptStn, String arrStn) {
        int dptStnIdx = stops.indexOf(dptStn);
        int arrStnIdx = stops.indexOf(arrStn);

        return stops.subList(dptStnIdx, arrStnIdx + 1);
    }
}
