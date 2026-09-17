package io.trainners.raily_backend.domain.notification.model.entity;

import io.trainners.raily_backend.domain.user.model.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_watches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeatWatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_seatwatch_user",
                    foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
            )
    )
    private User user;

    @Column(nullable = false)
    private String trainNumber;

    @Column(nullable = false)
    private String carNumber;

    @Column(nullable = false)
    private String seatNumber;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private SeatWatch(User user, String trainNumber, String carNumber, String seatNumber){
        this.user = user;
        this.trainNumber = trainNumber;
        this.carNumber = carNumber;
        this.seatNumber = seatNumber;
    }

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
    }
}
