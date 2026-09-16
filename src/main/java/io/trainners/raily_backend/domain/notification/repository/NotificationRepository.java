package io.trainners.raily_backend.domain.notification.repository;

import io.trainners.raily_backend.domain.notification.model.entity.SeatWatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<SeatWatch, Long> {
}
