package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.model.dto.PushMessage;
import io.trainners.raily_backend.domain.notification.model.entity.PushSubscription;
import io.trainners.raily_backend.domain.notification.repository.PushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.apache.http.HttpResponse;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;


// 한 사용자의 모든 기기로 푸시를 발송한다 (기기 하나가 실패해도 나머지 기기 발송을 막지 않음)
@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushSender {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PushService pushService;
    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final PushSubscriptionService pushSubscriptionService;

    public void send(Long userId, PushMessage message) {
        List<PushSubscription> subscriptions
                = pushSubscriptionRepository.findAllByUserId(userId);
        if (subscriptions.isEmpty()) {
            // 알림 권한을 거부했거나 아직 구독하지 않은 사용자. 알림함에는 이미 남아 있다
            log.debug("푸시 구독 없음 userId={}", userId);
            return;
        }

        String payload = OBJECT_MAPPER.writeValueAsString(message);

        for (PushSubscription subscription : subscriptions) {
            sendOne(subscription, payload);
        }
    }

    private void sendOne(PushSubscription subscription, String payload) {
        try {
            // 우리 엔티티 Notification 과 이름이 겹쳐 전체 경로로 쓴다
            nl.martijndwars.webpush.Notification notification =
                    new nl.martijndwars.webpush.Notification(
                            subscription.getEndpoint(),
                            subscription.getP256dh(),
                            subscription.getAuth(),
                            payload
                    );

            HttpResponse response = pushService.send(notification);
            int status = response.getStatusLine().getStatusCode();

            if (status == 404 || status == 410) {
                // 브라우저가 구독을 폐기한 상태. 계속 두면 매번 실패하므로 정리한다
                log.info("만료된 구독 삭제 endpoint={}", subscription.getEndpoint());
                pushSubscriptionService.deleteByEndpoint(subscription.getEndpoint());
            } else if (status >= 400) {
                log.warn("푸시 발송 실패 status={} endpoint={}", status, subscription.getEndpoint());
            }
        } catch (Exception e) {
            log.warn("푸시 발송 예외 endpoint={} : {}", subscription.getEndpoint(), e.toString());
        }
    }
}