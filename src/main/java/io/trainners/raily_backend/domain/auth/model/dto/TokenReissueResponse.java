package io.trainners.raily_backend.domain.auth.model.dto;

public record TokenReissueResponse(String accessToken, String email, String name) {
}
