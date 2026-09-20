package io.trainners.raily_backend.domain.notification.controller;


import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.service.SeatWatchService;
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

    // createSeatWatch가 SeatWatchService로 옮겨갔으므로 컨트롤러도 그 쪽을 보게 한다
    private final SeatWatchService seatWatchService;

    @PostMapping("/seat-watch")
    public ResponseEntity<Void> createSeatWatch(
            Authentication authentication,
            @RequestBody @Valid SeatWatchRequest request
    ){
        String email = authentication.getName();
        seatWatchService.create(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}

