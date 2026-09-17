package io.trainners.raily_backend.domain.auth.controller;

import io.trainners.raily_backend.domain.auth.model.dto.LoginRequest;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResponse;
import io.trainners.raily_backend.domain.auth.model.dto.LoginResult;
import io.trainners.raily_backend.domain.auth.model.dto.TokenReissueResponse;
import io.trainners.raily_backend.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletResponse response){
        LoginResult result = authService.login(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(Duration.ofDays(14))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(result.body());
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenReissueResponse> reissue(@CookieValue("refreshToken") String refreshToken){
        return ResponseEntity.ok(authService.reissue(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication, HttpServletResponse response){
        authService.logout(authentication.getName());

        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return ResponseEntity.noContent().build();
    }
}
