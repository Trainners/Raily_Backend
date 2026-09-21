package io.trainners.raily_backend.domain.notification.scheduler;

import io.trainners.raily_backend.domain.notification.model.dto.SeatCheckResult;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatchStatus;
import io.trainners.raily_backend.domain.notification.repository.SeatWatchRepository;
import io.trainners.raily_backend.domain.notification.service.SeatAvailabilityChecker;
import io.trainners.raily_backend.domain.notification.service.SeatWatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

// 1분마다 ACTIVE 상태인 감시 건을 훑는다.
// 대부분의 틱은 "열린 윈도우 없음"으로 끝나며, 그 경우 코레일을 호출하지 않는다.
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatWatchScheduler {

    private final SeatWatchRepository seatWatchRepository;
    private final SeatWatchService seatWatchService;
    private final SeatAvailabilityChecker seatAvailabilityChecker;

    // fixedDelay 를 쓰는 이유: 직전 실행이 "끝난 뒤"부터 60초를 센다.
    // 코레일 조회가 느려질 수 있는데 fixedRate 면 실행이 겹쳐 쌓일 수 있다.
    // 이 메서드에는 @Transactional 을 붙이지 않는다.
    // 감시 건마다 서비스가 각자 트랜잭션을 열어야, 한 건의 실패가 다른 건을 롤백시키지 않는다.
    // (@Transaction 붙이면 100건 중 99번째가 터졌을 때 앞의 98건 처리가 전부 롤백된다는 뜻)
    @Scheduled(fixedDelay = 60_000)
    public void checkActiveSeatWatches() {
        List<SeatWatch> actives = seatWatchRepository.findAllByStatus(SeatWatchStatus.ACTIVE);

        if (actives.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        log.debug("좌석 감시 검사 시작 count={}", actives.size());

        for (SeatWatch watch : actives) {
            try {
                process(watch, now);
            } catch (Exception e) {
                // 한 건이 터져도 나머지는 계속 처리한다
                log.warn("감시 처리 실패 watchId={} : {}", watch.getId(), e.toString());
            }
        }
    }

    private void process(SeatWatch watch, LocalDateTime now) {
        // 1) 여정이 끝났으면 더 팔릴 수 없다 -> 감시 종료
        if (watch.isJourneyOver(now)) {
            seatWatchService.expire(watch.getId());
            log.info("여정 종료로 감시 만료 watchId={}", watch.getId());
            return;
        }

        // 2) 지금 열려 있는 윈도우가 있는지 확인. 대부분의 틱이 여기서 끝난다.
        // 정차역 한 곳당 약 12분(도착 10분 전 ~ 출발)씩 코레일을 호출한다. 나머지 시간은 DB 조회 한 번으로 끝
        int stopIndex = watch.findStopIndexInWindow(now);
        if (stopIndex == -1) {
            return;
        }

        // 3) 윈도우가 열린 역 기준으로 좌석 판매 여부 확인 (여기서만 코레일을 호출한다)
        SeatCheckResult result = seatAvailabilityChecker.check(watch, stopIndex);
        if (!result.sold()) {
            return;
        }

        // 4) 판매 감지 -> 상태 전이 + 알림 저장 + 이벤트 발행 (커밋 후 푸시 발송)
        log.info("좌석 판매 감지 watchId={} station={}", watch.getId(), result.soldFromStation());
        seatWatchService.markSold(watch.getId(), result.soldFromStation());
    }
}