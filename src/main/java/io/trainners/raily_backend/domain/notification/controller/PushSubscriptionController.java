package io.trainners.raily_backend.domain.notification.controller;

import io.trainners.raily_backend.domain.notification.model.dto.PushSubscribeRequest;
import io.trainners.raily_backend.domain.notification.model.dto.PushUnsubscribeRequest;
import io.trainners.raily_backend.domain.notification.model.dto.VapidKeyResponse;
import io.trainners.raily_backend.domain.notification.service.PushSubscriptionService;
import io.trainners.raily_backend.domain.notification.service.WebPushSender;
import io.trainners.raily_backend.global.config.WebPushProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushSubscriptionController {

    private final WebPushProperties webPushProperties;
    private final PushSubscriptionService pushSubscriptionService;
    private final WebPushSender webPushSender;

    /**
     * 브라우저가 푸시 구독을 만들 때 필요한 VAPID 공개키.
     * 로그인 전에도 호출해야 해서 SecurityConfig 에서 permitAll 처리되어 있다.
     * 공개키는 노출돼도 안전하다. 서명에 쓰이는 건 서버만 가진 비밀키다.
     */
    @GetMapping("/vapid-public-key")
    public VapidKeyResponse getVapidPublicKey() {
        return new VapidKeyResponse(webPushProperties.publicKey());
    }

    /**
     * 구독 등록·갱신. 같은 기기에서 다시 호출해도 안전하다(endpoint 기준으로 갱신).
     */
    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void subscribe(
            Authentication authentication,
            @RequestBody @Valid PushSubscribeRequest request
    ) {
        pushSubscriptionService.subscribe(authentication.getName(), request);
    }

    /**
     * 구독 해제. 로그아웃 시 호출하지 않으면 같은 기기로 이전 계정의 알림이 갈 수 있다.
     * DELETE 에 본문을 싣는 이유: endpoint 가 URL 경로에 넣기엔 너무 길다.
     */
    @DeleteMapping("/subscriptions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(
            Authentication authentication,
            @RequestBody @Valid PushUnsubscribeRequest request
    ) {
        pushSubscriptionService.unsubscribe(authentication.getName(), request.endpoint());
    }

    /**
     * 개발·시연용. 내 기기로 테스트 알림을 보낸다.
     */
    @PostMapping("/test")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendTestPush(Authentication authentication) {
        webPushSender.sendTest(authentication.getName());
    }
}