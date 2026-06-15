package com.cibertec.edu.Api_Gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/productos")
    public ResponseEntity<Map<String, Object>> productosFallback() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "temporary_error");
        response.put("message", "El servicio de catálogo (productos) está temporalmente fuera de línea. Por favor, intente de nuevo más tarde.");
        response.put("data", new Object[]{});
        return ResponseEntity.status(503).body(response);
    }
}
