package com.pgrental.backend.controller;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.entity.Payment;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin
public class PaymentController {
    private final PaymentService paymentService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    private User getUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String phone = jwtUtils.getPhoneFromToken(header.substring(7));
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return paymentService.createOrder(body.get("bookingId"), getUser(request));
    }

    @PostMapping("/{id}/confirm")
    public Payment confirmPayment(@PathVariable String id,
                                   @RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        return paymentService.confirmPayment(id, body.get("razorpayPaymentId"), getUser(request));
    }

    @GetMapping
    public List<Payment> getMyPayments(HttpServletRequest request) {
        return paymentService.getUserPayments(getUser(request));
    }
}
