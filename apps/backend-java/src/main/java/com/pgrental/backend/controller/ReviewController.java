package com.pgrental.backend.controller;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.dto.ReviewDto;
import com.pgrental.backend.entity.Review;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Reviews", description = "PG reviews and ratings")
@RestController
@RequestMapping("/api/pgs/{pgId}/reviews")
@RequiredArgsConstructor
@CrossOrigin
public class ReviewController {
    private final ReviewService reviewService;
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

    @Operation(summary = "Get reviews for a PG")
    @GetMapping
    public List<Review> getReviews(@PathVariable String pgId) {
        return reviewService.getReviewsForPg(pgId);
    }

    @Operation(summary = "Submit a review for a PG")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review createReview(@PathVariable String pgId,
                                @RequestBody ReviewDto dto,
                                HttpServletRequest request) {
        return reviewService.createReview(pgId, getUser(request), dto);
    }
}
