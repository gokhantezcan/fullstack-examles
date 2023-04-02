package com.sha.springbootjwtauthorization.service;

import com.sha.springbootjwtauthorization.model.JwtRefreshToken;
import com.sha.springbootjwtauthorization.model.User;
import com.sha.springbootjwtauthorization.repository.JwtRefreshTokenRepository;
import com.sha.springbootjwtauthorization.repository.UserRepository;
import com.sha.springbootjwtauthorization.security.UserPrincipal;
import com.sha.springbootjwtauthorization.security.jwt.JwtProvider;
import com.sha.springbootjwtauthorization.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class JwtRefreshTokenServiceImp implements JwtRefreshTokenService {

    @Value("${app.jwt.refresh-expiration-in-ms}")
    private Long REFRESH_EXPIRATION_IN_MS;

    @Autowired
    private JwtRefreshTokenRepository jwtRefreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Override
    public JwtRefreshToken createRefreshToken(Long userId) {
        JwtRefreshToken jwtRefreshToken = new JwtRefreshToken();
        jwtRefreshToken.setTokenId(UUID.randomUUID().toString());
        jwtRefreshToken.setUserId(userId);
        jwtRefreshToken.setCreateDate(LocalDateTime.now());
        jwtRefreshToken.setExpirationDate(LocalDateTime.now().plus(REFRESH_EXPIRATION_IN_MS, ChronoUnit.MILLIS));
        return jwtRefreshTokenRepository.save(jwtRefreshToken);
    }

    @Override
    public User generateAccessTokenFromRefreshToken(String refreshTokenId) {
        Optional<JwtRefreshToken> jwtRefreshToken = jwtRefreshTokenRepository.findById(refreshTokenId);
        if (jwtRefreshToken.get().getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("JWT refresh token is not valid");
        }

        Optional<User> user = userRepository.findById(jwtRefreshToken.get().getUserId());
        UserPrincipal userPrincipal = UserPrincipal.builder()
                .id(user.get().getId())
                .username(user.get().getUsername())
                .password(user.get().getPassword())
                .authorities(Set.of(SecurityUtils.convertAuthority(user.get().getRole().name())))
                .build();

        String accessToken = jwtProvider.generateToken(userPrincipal);

        user.get().setAccessToken(accessToken);
        user.get().setRefreshToken(refreshTokenId);

        return user.get();
    }
}
