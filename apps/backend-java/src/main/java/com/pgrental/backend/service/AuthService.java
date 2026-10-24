package com.pgrental.backend.service;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.queue.OtpMessageProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtils jwtUtils;
    private final OtpMessageProducer otpMessageProducer;

    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_COOLDOWN_PREFIX = "otp_cooldown:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public void requestOtp(String phone, User.Role role, String name) {
        String cooldownKey = OTP_COOLDOWN_PREFIX + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new RuntimeException("Please wait 60 seconds before requesting another OTP");
        }
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
        redisTemplate.opsForValue().set(OTP_PREFIX + phone, otp, Duration.ofMinutes(5));
        // Cooldown: block re-request for 60s to prevent SMS flooding
        redisTemplate.opsForValue().set(cooldownKey, "1", Duration.ofSeconds(60));
        redisTemplate.delete(OTP_ATTEMPTS_PREFIX + phone);
        otpMessageProducer.sendOtpMessage(phone, otp);
    }

    public AuthResponse verifyOtp(String phone, String otp, User.Role role) {
        String attemptsKey = OTP_ATTEMPTS_PREFIX + phone;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, Duration.ofMinutes(5));
        }
        if (attempts > MAX_VERIFY_ATTEMPTS) {
            throw new RuntimeException("Too many failed attempts. Request a new OTP.");
        }

        String storedOtp = redisTemplate.opsForValue().get(OTP_PREFIX + phone);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        redisTemplate.delete(OTP_PREFIX + phone);
        redisTemplate.delete(attemptsKey);

        User user = userRepository.findByPhone(phone)
                .orElseGet(() -> userRepository.save(User.builder()
                        .phone(phone)
                        .role(role)
                        .build()));

        String token = jwtUtils.generateToken(phone);
        return new AuthResponse(user, new AuthTokens(token, null));
    }

    public record AuthTokens(String accessToken, String refreshToken) {}
    public record AuthResponse(User user, AuthTokens tokens) {}
}
