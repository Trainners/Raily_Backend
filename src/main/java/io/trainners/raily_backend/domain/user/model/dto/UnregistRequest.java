package io.trainners.raily_backend.domain.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnregistRequest {

    @NotBlank
    private final String password;
}
