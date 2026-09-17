package io.trainners.raily_backend.domain.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter // 모든 필드 읽기 전용 접근
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 외부에서 빈 객체 생성 방지
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String refreshToken;

    @Column
    private LocalDateTime refreshTokenExpiry;

    public void updateRefreshToken(String refreshToken, LocalDateTime expiry){
        this.refreshToken = refreshToken;
        this.refreshTokenExpiry = expiry;
    }

    public void clearRefreshToken(){
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }

    @Builder
    private User(String email, String password, String name){
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @PrePersist
    private void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public void changePassword(String encodedPassword){
        this.password = encodedPassword;
    }
}
