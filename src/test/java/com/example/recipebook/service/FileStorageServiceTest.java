package com.example.recipebook.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Безопасность загрузки файлов")
class FileStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Отклоняет файл, который не является изображением")
    void rejectsNonImage() {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "photosFiles", "shell.php", "application/x-php", "опасный файл".getBytes()
        );

        assertThatThrownBy(() -> storage.savePhotos(List.of(file)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Сохраняет изображение под безопасным именем")
    void storesImageWithSafeName() {
        FileStorageService storage = new FileStorageService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "photosFiles", "../../unsafe.php", "image/png", new byte[]{1, 2, 3}
        );

        String saved = storage.savePhotos(List.of(file)).get(0);

        assertThat(saved).matches("/uploads/[a-f0-9-]+\\.png");
        assertThat(Files.exists(tempDir.resolve(saved.substring(9)))).isTrue();
    }
}
