package com.example.bootheat.web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager/uploads")
public class UploadController {

    // 업로드 폴더 (현재 프로젝트 실행 폴더 기준)
    private final Path uploadDir = Paths.get("uploads");

    @PostMapping("/image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 업로드 폴더 없으면 자동 생성
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 2. 파일명 충돌 방지 (UUID 붙이기)
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new IllegalArgumentException("잘못된 파일명입니다.");
            }

            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path target = uploadDir.resolve(filename);

            // 3. 파일 저장 (기존 파일 있으면 덮어쓰기)
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // 4. URL 반환 (/uploads/파일명)
            return Map.of("url", "/uploads/" + filename);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }
}
