package com.pgrental.backend.repository;

import com.pgrental.backend.entity.PgImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PgImageRepository extends JpaRepository<PgImage, String> {
    List<PgImage> findByPgId(String pgId);
}
