package io.trainners.raily_backend.domain.auth.service;

import io.jsonwebtoken.Claims;
import io.trainners.raily_backend.domain.auth.jwt.JwtProvider;
import io.trainners.raily_backend.domain.auth.model.CustomUserDetails;
import io.trainners.raily_backend.domain.auth.model.dto.LoginRequest;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResponse;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResult;
import io.trainners.raily_backend.domain.auth.model.dto.TokenReissueResponse;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResult login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String accessToken = jwtProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtProvider.createRefreshToken(user.getEmail());

        user.updateRefreshToken(refreshToken, LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenValidity())));

        LoginResponse body = new LoginResponse(accessToken, user.getEmail(), user.getName());
        return new LoginResult(body, refreshToken);
    }

    public TokenReissueResponse reissue(String refreshToken){

        Claims claims;
        try{
            claims = jwtProvider.parseClaims(refreshToken);
        } catch(Exception e){
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if(!refreshToken.equals(user.getRefreshToken())){
            throw new RuntimeException("일치하지 않는 리프레시 토큰입니다.");
        }

        String newAccessToken = jwtProvider.createAccessToken(email);
        return new TokenReissueResponse(newAccessToken);
    }

    @Transactional
    public void logout(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        user.clearRefreshToken();
    }
}
