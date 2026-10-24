package com.pgrental.backend.controller;

import com.pgrental.backend.config.JwtUtils;
import com.pgrental.backend.entity.PgImage;
import com.pgrental.backend.entity.User;
import com.pgrental.backend.repository.UserRepository;
import com.pgrental.backend.service.ImageUploadService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@CrossOrigin
public class ImageController {
    private final ImageUploadService imageUploadService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private User getUser(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String phone = jwtUtils.getPhoneFromToken(header.substring(7));
        return userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/api/pgs/{pgId}/images")
    public List<PgImage> getImages(@PathVariable String pgId) {
        return imageUploadService.getImagesForPg(pgId);
    }

    @PostMapping("/api/pgs/{pgId}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public PgImage uploadImage(@PathVariable String pgId,
                                @RequestParam("file") MultipartFile file,
                                HttpServletRequest request) throws IOException {
        return imageUploadService.uploadImage(pgId, getUser(request), file);
    }

    @DeleteMapping("/api/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable String imageId, HttpServletRequest request) throws IOException {
        imageUploadService.deleteImage(imageId, getUser(request));
    }

    @GetMapping("/api/files/{pgId}/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String pgId,
                                               @PathVariable String filename) throws IOException {
        Path file = Paths.get(uploadDir, pgId, filename);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String contentType = "image/jpeg";
        if (filename.endsWith(".png")) contentType = "image/png";
        else if (filename.endsWith(".webp")) contentType = "image/webp";
        else if (filename.endsWith(".gif")) contentType = "image/gif";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
