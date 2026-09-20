package io.trainners.raily_backend.domain.seat.service;

import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsApiResponse;
import io.trainners.raily_backend.domain.korail.dto.AvailableSeatsResponse;
import io.trainners.raily_backend.domain.seat.model.SeatOption;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeatRecommendationService {

    public List<SeatOption> recommend(Map<String, Map<String, AvailableSeatsApiResponse>> segmentSeatStatus) {
        List<SeatOption> seatOptions = assemble(segmentSeatStatus);
        seatOptions.sort(SeatRecommendationService::comparePriority);
        return seatOptions;
    }

    private List<SeatOption> assemble(Map<String, Map<String, AvailableSeatsApiResponse>> segmentSeatStatus) {
        List<String> orderedSegments = new ArrayList<>(segmentSeatStatus.keySet());
        int totalSegments = orderedSegments.size();

        Map<String, boolean[]> availabilityByKey = new LinkedHashMap<>();
        Map<String, String[]> seatIdentityByKey = new LinkedHashMap<>(); // key -> [호차, 좌석번호]

        for (int segmentIndex = 0; segmentIndex < totalSegments; segmentIndex++) {
            Map<String, AvailableSeatsApiResponse> carsInSegment =
                    segmentSeatStatus.get(orderedSegments.get(segmentIndex));

            for (Map.Entry<String, AvailableSeatsApiResponse> carEntry : carsInSegment.entrySet()) {
                String carNumber = carEntry.getKey();

                for (AvailableSeatsResponse seat : extractSeats(carEntry.getValue())) {
                    String key = carNumber + "-" + seat.getSeatNumber();
                    seatIdentityByKey.putIfAbsent(key, new String[]{carNumber, seat.getSeatNumber()});

                    boolean[] availability =
                            availabilityByKey.computeIfAbsent(key, k -> new boolean[totalSegments]);
                    availability[segmentIndex] = "Y".equals(seat.getIsSellable());
                }
            }
            // 이 구간에 등장하지 않은 호차(= 잔여석 0이라 조회조차 안 한 호차)의 좌석은
            // boolean[] 기본값 false로 남는다 = 그 구간에선 못 앉음
        }

        List<SeatOption> result = new ArrayList<>();
        for (Map.Entry<String, boolean[]> entry : availabilityByKey.entrySet()) {
            String[] identity = seatIdentityByKey.get(entry.getKey());

            List<Boolean> availability = new ArrayList<>();
            for (boolean available : entry.getValue()) {
                availability.add(available);
            }

            SeatOption option = new SeatOption(identity[0], identity[1], availability);
            if (option.getAvailableSegmentCount() > 0) { // 전 구간 매진된 좌석은 내려줄 이유가 없음
                result.add(option);
            }
        }
        return result;
    }

    private List<AvailableSeatsResponse> extractSeats(AvailableSeatsApiResponse response) {
        if (response == null || response.getSeatInfos() == null
                || response.getSeatInfos().getSeatInfoList() == null) {
            return List.of();
        }
        return response.getSeatInfos().getSeatInfoList();
    }

    /* 좌석 우선순위 (위에서부터 순서대로 비교)
       1. 지금부터 연속으로 앉을 수 있는 구간 수  ← 0이면 지금 못 앉는 자리라 자동으로 뒤로 밀림
       2. 여정 전체에서의 최장 연속 구간 수
       3. 전체 가용 구간 수
       4. 호차 → 좌석번호 오름차순 (동점일 때 결과가 매번 흔들리지 않게) */
    private static int comparePriority(SeatOption a, SeatOption b) {
        int byInitialRun = Integer.compare(b.getInitialContiguousRun(), a.getInitialContiguousRun());
        if (byInitialRun != 0) {
            return byInitialRun;
        }

        int byLongestRun = Integer.compare(b.getLongestContiguousRun(), a.getLongestContiguousRun());
        if (byLongestRun != 0) {
            return byLongestRun;
        }

        int byAvailableCount = Integer.compare(b.getAvailableSegmentCount(), a.getAvailableSegmentCount());
        if (byAvailableCount != 0) {
            return byAvailableCount;
        }

        int byCarNumber = a.getCarNumber().compareTo(b.getCarNumber());
        if (byCarNumber != 0) {
            return byCarNumber;
        }
        return a.getSeatNumber().compareTo(b.getSeatNumber());
    }
}