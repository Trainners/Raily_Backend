package io.trainners.raily_backend.domain.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank
    @Email
    private final String email;

    @NotBlank
    @Size(min = 8, max = 64)
    private final String password;

    @NotBlank
    @Size(max = 30)
    private final String name;
}
