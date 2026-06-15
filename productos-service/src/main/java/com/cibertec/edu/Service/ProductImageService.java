package com.cibertec.edu.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductImageService {
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path uploadDirectory;

    public ProductImageService(@Value("${app.upload.dir:uploads/productos}") String uploadDir) {
        this.uploadDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la carpeta de imágenes.", e);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Seleccione una imagen.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB.");
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("Formato no permitido. Use JPG, PNG, WEBP o GIF.");
        }

        String filename = UUID.randomUUID() + extension;
        Path destination = uploadDirectory.resolve(filename).normalize();
        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la imagen.", e);
        }
    }

    public Resource load(String filename) {
        Path file = uploadDirectory.resolve(filename).normalize();
        if (!file.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException("Nombre de archivo inválido.");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalArgumentException("Imagen no encontrada.");
            }
            return resource;
        } catch (IOException e) {
            throw new IllegalArgumentException("Imagen no encontrada.", e);
        }
    }
}
