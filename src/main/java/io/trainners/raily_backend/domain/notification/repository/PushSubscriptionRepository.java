package io.trainners.raily_backend.domain.notification.repository;

import io.trainners.raily_backend.domain.notification.model.entity.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    // 구독 등록 시 이 기기가 등록돼있는지 여부 확인 -> 있으면 갱신, 없으면 새로 저장
    Optional<PushSubscription> findByEndpoint(String endpoint);

    // 알림 보낼 때 그 사용자의 모든 기기를 가져오기 위함
    List<PushSubscription> findAllByUserId(Long userId);

    // 로그아웃/알림 끄기 시 내 기기만 삭제 (email로 소유자 검사)
    void deleteByEndpointAndUserEmail(String endpoint, String email);

    // 푸시 발송 시 404 / 410 만료 응답을 받은 구독 정리 (소유자 무관)
    void deleteByEndpoint(String endpoint);

    // 테스트 푸시: 이메일로 내 기기 전체 조회
    List<PushSubscription> findAllByUserEmail(String email);
}