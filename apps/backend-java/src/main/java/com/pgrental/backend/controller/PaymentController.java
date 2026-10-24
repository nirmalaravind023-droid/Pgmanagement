package com.pgrental.backend.controller;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.entity.Payment;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Payments", description = "Razorpay payment integration")
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

    @Operation(summary = "Create Razorpay payment order")
    @PostMapping("/create-order")
    public Map<String, Object> createOrder(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return paymentService.createOrder(body.get("bookingId"), getUser(request));
    }

    @Operation(summary = "Confirm payment after Razorpay callback")
    @PostMapping("/{id}/confirm")
    public Payment confirmPayment(@PathVariable String id,
                                   @RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        return paymentService.confirmPayment(id, body.get("razorpayPaymentId"), getUser(request));
    }

    @Operation(summary = "Get current user's payment history")
    @GetMapping
    public List<Payment> getMyPayments(HttpServletRequest request) {
        return paymentService.getUserPayments(getUser(request));
    }
}
