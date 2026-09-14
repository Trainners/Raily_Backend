package io.trainners.raily_backend.domain.auth.service;

import io.trainners.raily_backend.domain.auth.jwt.JwtProvider;
import io.trainners.raily_backend.domain.auth.model.dto.LoginRequest;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtProvider.createToken(request.getEmail());
        return new LoginResponse(accessToken);
    }
}
