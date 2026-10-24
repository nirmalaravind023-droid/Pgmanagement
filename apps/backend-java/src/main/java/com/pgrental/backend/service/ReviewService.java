package com.pgrental.backend.service;

import com.pgrental.backend.dto.ReviewDto;
import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.Review;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.PgRepository;
import com.pgrental.backend.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PgRepository pgRepository;

    @Transactional(readOnly = true)
    public List<Review> getReviewsForPg(String pgId) {
        return reviewRepository.findByPgId(pgId);
    }

    @Transactional
    public Review createReview(String pgId, User user, ReviewDto dto) {
        if (reviewRepository.existsByPgIdAndUserId(pgId, user.getId())) {
            throw new RuntimeException("You have already reviewed this PG");
        }
        Pg pg = pgRepository.findById(pgId)
                .orElseThrow(() -> new RuntimeException("PG not found"));

        Review review = Review.builder()
                .pg(pg)
                .user(user)
                .overallRating(dto.getOverallRating())
                .foodRating(dto.getFoodRating())
                .safetyRating(dto.getSafetyRating())
                .cleanlinessRating(dto.getCleanlinessRating())
                .comment(dto.getComment())
                .build();
        review = reviewRepository.save(review);

        recalculatePgRating(pgId, pg);
        return review;
    }

    private void recalculatePgRating(String pgId, Pg pg) {
        List<Review> reviews = reviewRepository.findByPgId(pgId);
        if (reviews.isEmpty()) return;
        double avg = reviews.stream()
                .mapToInt(Review::getOverallRating)
                .average()
                .orElse(0.0);
        pg.setOverallRating(avg);
        pgRepository.save(pg);
    }
}
