package io.trainners.raily_backend.domain.auth.service;

import io.trainners.raily_backend.domain.auth.model.CustomUserDetails;
import io.trainners.raily_backend.domain.user.model.entity.User;
import io.trainners.raily_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        return new CustomUserDetails(user);
    }
}
