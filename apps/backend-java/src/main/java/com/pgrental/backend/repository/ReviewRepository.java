package com.pgrental.backend.repository;

import com.pgrental.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
    List<Review> findByPgId(String pgId);
    List<Review> findByUserId(String userId);
    boolean existsByPgIdAndUserId(String pgId, String userId);
}
