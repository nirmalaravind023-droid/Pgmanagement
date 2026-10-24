package com.pgrental.backend.service;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.queue.OtpMessageProducer;
import com.pgrental.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock StringRedisTemplate redisTemplate;
    @Mock JwtUtils jwtUtils;
    @Mock OtpMessageProducer otpMessageProducer;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks AuthService authService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void requestOtp_storesOtpAndPublishes() {
        String phone = "+919999999999";
        String result = authService.requestOtp(phone, User.Role.STUDENT, "Test User");

        assertThat(result).hasSize(4);
        verify(valueOps).set(eq("otp:" + phone), anyString(), any());
        verify(otpMessageProducer).sendOtpMessage(eq(phone), anyString());
    }

    @Test
    void verifyOtp_existingUser_returnsToken() {
        String phone = "+919999999999";
        String otp = "1234";
        User existing = User.builder().id("u1").phone(phone).role(User.Role.STUDENT).build();

        when(valueOps.get("otp:" + phone)).thenReturn(otp);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(existing));
        when(jwtUtils.generateToken(phone)).thenReturn("jwt-token");

        AuthService.AuthResponse response = authService.verifyOtp(phone, otp, User.Role.STUDENT);

        assertThat(response.user()).isEqualTo(existing);
        assertThat(response.tokens().accessToken()).isEqualTo("jwt-token");
        verify(redisTemplate).delete("otp:" + phone);
    }

    @Test
    void verifyOtp_newUser_createsUser() {
        String phone = "+919988888888";
        String otp = "5678";

        when(valueOps.get("otp:" + phone)).thenReturn(otp);
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtUtils.generateToken(phone)).thenReturn("new-token");

        AuthService.AuthResponse response = authService.verifyOtp(phone, otp, User.Role.STUDENT);

        assertThat(response.user().getPhone()).isEqualTo(phone);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void verifyOtp_wrongOtp_throws() {
        String phone = "+919999999999";
        when(valueOps.get("otp:" + phone)).thenReturn("9999");

        assertThatThrownBy(() -> authService.verifyOtp(phone, "1111", User.Role.STUDENT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired OTP");
    }

    @Test
    void verifyOtp_nullOtp_throws() {
        String phone = "+919999999999";
        when(valueOps.get("otp:" + phone)).thenReturn(null);

        assertThatThrownBy(() -> authService.verifyOtp(phone, "1234", User.Role.STUDENT))
                .isInstanceOf(RuntimeException.class);
    }
}
