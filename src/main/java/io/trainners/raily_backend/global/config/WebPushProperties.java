package io.trainners.raily_backend.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.yml의 webpush.vapid.* 값을 타입 안전하게 받는 설정 객체
// record로 만들면 생성자 바인딩이 자동으로 된다.
@ConfigurationProperties(prefix = "webpush.vapid")
public record WebPushProperties(
        String publicKey,
        String privateKey,
        String subject
) {
}
