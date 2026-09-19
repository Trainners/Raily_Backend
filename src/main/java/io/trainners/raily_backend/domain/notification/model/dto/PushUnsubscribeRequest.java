package io.trainners.raily_backend.domain.notification.model.dto;

import jakarta.validation.constraints.NotBlank;

public record PushUnsubscribeRequest(
        @NotBlank(message = "구독 endpoint는 필수입니다.")
        String endpoint
) {
}
