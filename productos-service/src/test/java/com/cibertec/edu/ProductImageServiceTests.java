package com.cibertec.edu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.cibertec.edu.Service.ProductImageService;

class ProductImageServiceTests {
    @TempDir
    Path tempDirectory;

    @Test
    void guardaYCargaImagenValida() throws Exception {
        ProductImageService service = new ProductImageService(tempDirectory.toString());
        MockMultipartFile image = new MockMultipartFile(
                "file", "producto.png", "image/png", new byte[] { 1, 2, 3 });

        String filename = service.save(image);

        assertTrue(filename.endsWith(".png"));
        assertTrue(Files.exists(tempDirectory.resolve(filename)));
        assertEquals(3, service.load(filename).contentLength());
    }

    @Test
    void rechazaArchivoQueNoEsImagen() {
        ProductImageService service = new ProductImageService(tempDirectory.toString());
        MockMultipartFile text = new MockMultipartFile(
                "file", "notas.txt", "text/plain", "contenido".getBytes());

        assertThrows(IllegalArgumentException.class, () -> service.save(text));
    }
}
