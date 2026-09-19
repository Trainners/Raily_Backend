package io.trainners.raily_backend.domain.notification.model.dto;

// HTTP로 노출되지 않고 서비스끼리 주고받는 객체
// 좌석 확인 결과를 "팔렸는지 + 어느 역부터"로 묶어 돌려줌
public record SeatCheckResult(
        boolean sold,
        String soldFromStation // 판매되지 않았으면 null
) {
    // 정적 팩토리 2개 (객체의 생성을 담당하는 클래스 메서드)
    // 아직 비어있는 경우
    public static SeatCheckResult stillFree() {
        return new SeatCheckResult(false, null);
    }

    // 해당 역부터 판매됨
    public static SeatCheckResult soldFrom(String station) {
        return new SeatCheckResult(true, station);
    }
}
