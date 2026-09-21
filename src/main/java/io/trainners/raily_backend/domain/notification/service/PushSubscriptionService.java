package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.model.dto.PushSubscribeRequest;
import io.trainners.raily_backend.domain.notification.model.entity.PushSubscription;
import io.trainners.raily_backend.domain.notification.repository.PushSubscriptionRepository;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushSubscriptionService {
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final UserRepository userRepository;

    // 브라우저가 만든 푸시 구독 정보 저장
    // endpoint에 unique 제약이 있어 같은 기기가 다시 구독해도 행이 늘지 않도록 갱신/저장을 분기함
    @Transactional
    public void subscribe(String email, PushSubscribeRequest request) {
        // 구독의 소유자
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        pushSubscriptionRepository.findByEndpoint(request.endpoint()) // 기존 행 조회
                .ifPresentOrElse(
                        // [있을 때] 이미 등록된 기기 -> 소유자와 키를 갱신
                        // 조회된 엔티티는 영속 상태라 save() 없이 더티 체킹으로 UPDATE가 나간다.
                        // 소유자까지 갱신하는 이유: 한 기기를 여러 계정이 번갈아 쓸 수 있고,
                        // 갱신하지 않으면 이전 계정 앞으로 알림이 간다.
                        subscription -> subscription.update(
                                user,
                                request.keys().p256dh(),
                                request.keys().auth()
                        ),
                        // [없을 때] 처음 보는 기기 -> 새 행으로 저장
                        // 넘겨줄 기존 객체가 없으므로 인자 없는 람다 () -> ... 형태가 된다
                        () -> pushSubscriptionRepository.save(
                                PushSubscription.builder()
                                        .user(user)
                                        .endpoint(request.endpoint())
                                        .p256dh(request.keys().p256dh())
                                        .auth(request.keys().auth())
                                        .build()
                        )
                );
    }

    // 로그아웃 / 알림 끄기 시 호출
    // email 조건으로 남의 구독 못 지우게 함
    // 없는 구독 지워도 예외 X
    @Transactional
    public void unsubscribe(String email, String endpoint) {
        pushSubscriptionRepository.deleteByEndpointAndUserEmail(endpoint, email);
    }

    // 푸시 발송 중 404 / 410 응답을 받은 만료 구독 정리 (WebPushSender가 호출)
    // 푸시 서버가 "이 endpoint는 죽었다"고 알려준 상황이므로 소유자 확인 X
    @Transactional
    public void deleteByEndpoint(String endpoint){
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

}
