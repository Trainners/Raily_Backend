package io.trainners.raily_backend.domain.user.service;

import io.trainners.raily_backend.domain.user.model.dto.SignUpRequest;
import io.trainners.raily_backend.domain.user.model.dto.UnregistRequest;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import io.trainners.raily_backend.global.exception.BusinessException;
import io.trainners.raily_backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(SignUpRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        return userRepository.save(user).getId();
    }

    @Transactional
    public void withdraw(String email, UnregistRequest request){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        userRepository.delete(user);
    }
}
