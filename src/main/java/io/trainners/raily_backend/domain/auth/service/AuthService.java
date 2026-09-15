package io.trainners.raily_backend.domain.auth.service;

import io.trainners.raily_backend.domain.auth.jwt.JwtProvider;
import io.trainners.raily_backend.domain.auth.model.CustomUserDetails;
import io.trainners.raily_backend.domain.auth.model.dto.LoginRequest;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResponse;
import io.trainners.raily_backend.domain.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtProvider.createToken(user.getEmail());
        return new LoginResponse(accessToken, user.getEmail(), user.getName());
    }
}
