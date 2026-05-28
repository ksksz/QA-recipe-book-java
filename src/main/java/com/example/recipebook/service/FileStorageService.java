package com.example.recipebook.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path uploadsPath;

    public FileStorageService(@Value("${recipebook.uploads-path}") String uploadsPath) {
        this.uploadsPath = Path.of(uploadsPath);
    }

    public List<String> savePhotos(List<MultipartFile> files) {
        List<String> paths = new ArrayList<>();
        try {
            Files.createDirectories(uploadsPath);
            for (MultipartFile file : files == null ? List.<MultipartFile>of() : files) {
                if (file == null || file.isEmpty()) continue;
                String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
                String extension = "";
                int dot = original.lastIndexOf('.');
                if (dot >= 0) extension = original.substring(dot);
                String filename = UUID.randomUUID() + extension;
                file.transferTo(uploadsPath.resolve(filename));
                paths.add("/uploads/" + filename);
            }
            return paths;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot save uploaded file", e);
        }
    }
}
