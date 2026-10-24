package com.pgrental.backend.controller;

import com.pgrental.backend.dto.CreatePgRequest;
import com.pgrental.backend.entity.Booking;
import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.OwnerService;
import com.pgrental.backend.config.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Owner Dashboard", description = "Owner PG management")
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

    @Operation(summary = "Get owner's PG listings")
    @GetMapping("/pgs")
    public List<Pg> getMyPgs(HttpServletRequest request) {
        return ownerService.getOwnerPgs(getUser(request));
    }

    @Operation(summary = "Create a new PG listing")
    @PostMapping("/pgs")
    @ResponseStatus(HttpStatus.CREATED)
    public Pg createPg(@RequestBody CreatePgRequest req, HttpServletRequest request) {
        return ownerService.createPg(getUser(request), req);
    }

    @Operation(summary = "Update a PG listing")
    @PutMapping("/pgs/{id}")
    public Pg updatePg(@PathVariable String id, @RequestBody CreatePgRequest req, HttpServletRequest request) {
        return ownerService.updatePg(id, getUser(request), req);
    }

    @Operation(summary = "Delete a PG listing")
    @DeleteMapping("/pgs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePg(@PathVariable String id, HttpServletRequest request) {
        ownerService.deletePg(id, getUser(request));
    }

    @Operation(summary = "Get bookings for owner's PGs")
    @GetMapping("/bookings")
    public List<Booking> getMyBookings(HttpServletRequest request) {
        return ownerService.getOwnerBookings(getUser(request));
    }
}
