package io.trainners.raily_backend.domain.notification.service;

import io.trainners.raily_backend.domain.notification.model.dto.SeatWatchRequest;
import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import io.trainners.raily_backend.domain.notification.repository.NotificationRepository;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createSeatWatch(String email, SeatWatchRequest request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("존재하지 않는 사용자입니다."));

        SeatWatch seatWatch = SeatWatch.builder()
                .user(user)
                .trainNumber(request.getTrainNumber())
                .carNumber(request.getCarNumber())
                .seatNumber(request.getSeatNumber())
                .build();

        notificationRepository.save(seatWatch);
    }

}
