package com.pgrental.backend.service;

import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.Payment;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.BookingRepository;
import com.pgrental.backend.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayClient razorpayClient;

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Transactional
    public Map<String, Object> createOrder(String bookingId, User user) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        int amountPaise = booking.getPg().getRent() * 100; // Razorpay uses paise
        String orderId;

        if (razorpayKeyId.contains("placeholder")) {
            orderId = "order_demo_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 14);
            log.info("[Payment] Demo mode — mock order: {}", orderId);
        } else {
            try {
                JSONObject options = new JSONObject();
                options.put("amount", amountPaise);
                options.put("currency", "INR");
                options.put("receipt", bookingId);
                Order order = razorpayClient.orders.create(options);
                orderId = order.get("id");
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
            }
        }

        Payment payment = Payment.builder()
                .booking(booking)
                .user(user)
                .razorpayOrderId(orderId)
                .amount(amountPaise)
                .status("PENDING")
                .build();
        payment = paymentRepository.save(payment);

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("orderId", orderId);
        result.put("keyId", razorpayKeyId);
        result.put("amount", amountPaise);
        result.put("currency", "INR");
        return result;
    }

    @Transactional
    public Payment confirmPayment(String paymentId, String razorpayPaymentId, User user) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Forbidden");
        }
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus("SUCCESS");
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserId(user.getId());
    }
}
