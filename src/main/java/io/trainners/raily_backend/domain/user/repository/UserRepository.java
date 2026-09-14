package io.trainners.raily_backend.domain.user.repository;

import io.trainners.raily_backend.domain.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 회원 탈퇴 시: 이 이메일로 활성 상태인 유저 찾기 -> 있으면 탈퇴(없으면 에러)
    Optional<User> findByEmail(String email);

    // 회원 가입 시: 이메일 중복 체크용
    boolean existsByEmail(String email);
}
