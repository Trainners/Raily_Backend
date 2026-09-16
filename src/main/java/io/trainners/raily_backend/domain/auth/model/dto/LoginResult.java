package io.trainners.raily_backend.domain.auth.model.dto;

public record LoginResult(LoginResponse body, String refreshToken) {
}
