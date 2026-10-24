package com.pgrental.backend.controller;

import com.pgrental.backend.dto.CreatePgRequest;
import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.OwnerService;
import com.pgrental.backend.config.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
@CrossOrigin
public class OwnerController {
    private final OwnerService ownerService;
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

    @GetMapping("/pgs")
    public List<Pg> getMyPgs(HttpServletRequest request) {
        return ownerService.getOwnerPgs(getUser(request));
    }

    @PostMapping("/pgs")
    @ResponseStatus(HttpStatus.CREATED)
    public Pg createPg(@RequestBody CreatePgRequest req, HttpServletRequest request) {
        return ownerService.createPg(getUser(request), req);
    }

    @PutMapping("/pgs/{id}")
    public Pg updatePg(@PathVariable String id, @RequestBody CreatePgRequest req, HttpServletRequest request) {
        return ownerService.updatePg(id, getUser(request), req);
    }

    @DeleteMapping("/pgs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePg(@PathVariable String id, HttpServletRequest request) {
        ownerService.deletePg(id, getUser(request));
    }

    @GetMapping("/bookings")
    public List<Booking> getMyBookings(HttpServletRequest request) {
        return ownerService.getOwnerBookings(getUser(request));
    }
}
