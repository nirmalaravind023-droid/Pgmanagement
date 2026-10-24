package com.pgrental.backend.controller;

import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.RoomInventory;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.service.BookingService;
import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin
public class BookingController {
    private final BookingService bookingService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    private String getPhone(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        return jwtUtils.getPhoneFromToken(header.substring(7));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String phone = getPhone(request);
        String pgId = body.get("pgId");
        RoomInventory.RoomType roomType = RoomInventory.RoomType.valueOf(body.get("roomType").toUpperCase());
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookingService.createBooking(user, pgId, roomType);
    }

    @GetMapping
    public List<Booking> getUserBookings(HttpServletRequest request) {
        String phone = getPhone(request);
        return bookingService.getUserBookings(phone);
    }

    @DeleteMapping("/{id}")
    public Booking cancelBooking(@PathVariable String id, HttpServletRequest request) {
        String phone = getPhone(request);
        return bookingService.cancelBooking(id, phone);
    }
}
