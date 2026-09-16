package io.trainners.raily_backend.domain.notification.controller;


import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/seat-watch")
    public ResponseEntity<Void> createSeatWatch(
            Authentication authentication,
            @RequestBody @Valid SeatWatchRequest request
    ){
        String email = authentication.getName();
        notificationService.createSeatWatch(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

