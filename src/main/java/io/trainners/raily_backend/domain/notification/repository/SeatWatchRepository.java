package io.trainners.raily_backend.domain.notification.repository;

import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// 좌석 감시 엔티티를 다루는 레포지토리이므로 NotificationRepository에서 SeatWatchRepository로 Rename 했습니다.
public interface SeatWatchRepository extends JpaRepository<SeatWatch, Long> {
    // 스케줄러가 ACTIVE인 감시건만 뽑을 때
    List<SeatWatch> findAllByStatus(SeatWatchStatus status);
    // 사용자가 새로 앉을 떄, 그 사람의 기존 ACTIVE 감시를 취소하는 용도
    List<SeatWatch> findAllByUserEmailAndStatus(String email, SeatWatchStatus status);
    // id로 조회하되, 소유자 email까지 같이 검사해 남의 감시건은 못 보게 하는 용도
    Optional<SeatWatch> findByIdAndUserEmail(Long id, String email);
}
