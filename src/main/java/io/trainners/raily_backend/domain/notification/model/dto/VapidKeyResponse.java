package io.trainners.raily_backend.domain.notification.model.dto;

public record VapidKeyResponse(
        String publicKey // 응답은 객체로 내리는 게 안전하므로 문자열 하나여도 감쌌다.
) {
}
