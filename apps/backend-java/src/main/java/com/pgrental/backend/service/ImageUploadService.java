package com.pgrental.backend.service;

import com.pgrental.backend.entity.Pg;
import com.pgrental.backend.entity.PgImage;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.PgImageRepository;
import com.pgrental.backend.repository.PgRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageUploadService {
    private final PgImageRepository pgImageRepository;
    private final PgRepository pgRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<PgImage> getImagesForPg(String pgId) {
        return pgImageRepository.findByPgId(pgId);
    }

    @Transactional
    public PgImage uploadImage(String pgId, User owner, MultipartFile file) throws IOException {
        Pg pg = pgRepository.findById(pgId)
                .orElseThrow(() -> new RuntimeException("PG not found"));
        if (!pg.getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden: not your PG");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";
        String filename = pgId + "/" + UUID.randomUUID() + ext;

        Path dir = Paths.get(uploadDir, pgId);
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), Paths.get(uploadDir, filename));

        boolean isPrimary = pgImageRepository.findByPgId(pgId).isEmpty();
        PgImage image = PgImage.builder()
                .pg(pg)
                .filename(filename)
                .isPrimary(isPrimary)
                .build();
        return pgImageRepository.save(image);
    }

    @Transactional
    public void deleteImage(String imageId, User owner) throws IOException {
        PgImage image = pgImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));
        if (!image.getPg().getOwner().getId().equals(owner.getId())) {
            throw new RuntimeException("Forbidden: not your PG");
        }
        Path file = Paths.get(uploadDir, image.getFilename());
        Files.deleteIfExists(file);
        pgImageRepository.delete(image);
    }
}
