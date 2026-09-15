package io.trainners.raily_backend.domain.user.controller;

import io.trainners.raily_backend.domain.user.model.dto.SignUpRequest;
import io.trainners.raily_backend.domain.user.model.dto.UnregistRequest;
import io.trainners.raily_backend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@RequestBody @Valid SignUpRequest request){
        Long userId = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(Authentication authentication, @RequestBody @Valid UnregistRequest request){
        String email = authentication.getName();
        userService.withdraw(email, request);
        return ResponseEntity.noContent().build();
    }
}


