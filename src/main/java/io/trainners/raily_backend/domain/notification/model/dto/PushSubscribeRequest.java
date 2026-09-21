package io.trainners.raily_backend.domain.notification.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// 브라우저가 만들어주는 구독 객체를 그대로 받음
/*
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/...",
  "keys": { "p256dh": "...", "auth": "..." } // 중첩된 객체이므로 record 안에 record를 넣는다.
}
*/
@JsonIgnoreProperties(ignoreUnknown = true) // 브라우저가 보내는 expirationTime 필드 무시
public record PushSubscribeRequest(
        @NotBlank(message = "구독 endpoint는 필수입니다.")
        String endpoint,
        @NotNull(message = "구독 키 정보는 필수입니다.")
        @Valid
        Keys keys
) {
    public record Keys(
            @NotBlank(message = "p256dh 키는 필수입니다.")
            String p256dh,
            @NotBlank(message = "auth 키는 필수입니다.")
            String auth
    ) {}
}
