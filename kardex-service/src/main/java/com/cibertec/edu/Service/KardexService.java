package com.cibertec.edu.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.transaction.Transactional;

import com.cibertec.edu.Models.Kardex;
import com.cibertec.edu.Repository.IKardexRepository;

@Service
public class KardexService {

    private final IKardexRepository kardexRepository;
    private final RestTemplate restTemplate;
    private final String productosServiceUri;

    public KardexService(IKardexRepository kardexRepository,
                         RestTemplate restTemplate,
                         @Value("${app.services.productos-uri:http://localhost:8081}") String productosServiceUri) {
        this.kardexRepository = kardexRepository;
        this.restTemplate = restTemplate;
        this.productosServiceUri = productosServiceUri;
    }

    public List<Kardex> getAllKardex() {
        return kardexRepository.findAllByOrderByFechaDesc();
    }

    public List<Kardex> getKardexByProducto(Long idProducto) {
        return kardexRepository.findByIdProductoOrderByFechaDesc(idProducto);
    }

    @Transactional
    public Kardex registrarMovimiento(Kardex movimiento) {
        Long idProducto = movimiento.getIdProducto();
        
        // Fetch current product from productos-service via LoadBalanced RestTemplate
        String productUrl = productosServiceUri + "/api/productos/" + idProducto;
        Map<String, Object> product;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(productUrl, HttpMethod.GET, authorizedEntity(null), Map.class);
            product = response.getBody();
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo obtener el producto con ID: " + idProducto + ". El servicio de productos no responde.");
        }

        if (product == null || !product.containsKey("stock")) {
            throw new IllegalArgumentException("El producto con ID: " + idProducto + " no existe.");
        }

        int stockAnterior = ((Number) product.get("stock")).intValue();
        int stockPosterior = stockAnterior;
        if (movimiento.getCantidad() == null) {
            throw new IllegalArgumentException("La cantidad es requerida.");
        }
        int cantidad = movimiento.getCantidad();

        if (movimiento.getTipoMovimiento() == null) {
            throw new IllegalArgumentException("El tipo de movimiento es requerido.");
        }

        switch (movimiento.getTipoMovimiento().toUpperCase()) {
            case "ENTRADA":
                if (cantidad <= 0) {
                    throw new IllegalArgumentException("La cantidad de entrada debe ser mayor que cero.");
                }
                stockPosterior = stockAnterior + cantidad;
                break;
            case "SALIDA":
                if (cantidad <= 0) {
                    throw new IllegalArgumentException("La cantidad de salida debe ser mayor que cero.");
                }
                if (stockAnterior < cantidad) {
                    throw new IllegalArgumentException("Stock insuficiente para realizar esta salida. Stock actual: " + stockAnterior);
                }
                stockPosterior = stockAnterior - cantidad;
                break;
            case "AJUSTE":
                if (cantidad < 0) {
                    throw new IllegalArgumentException("El nuevo stock no puede ser negativo.");
                }
                stockPosterior = cantidad; // El valor ingresado es el nuevo stock
                // La diferencia se guarda en cantidad para el registro del kardex
                movimiento.setCantidad(Math.abs(stockPosterior - stockAnterior));
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento inválido: " + movimiento.getTipoMovimiento());
        }

        // Update the product's stock in productos-service
        Map<String, Object> stockUpdate = Map.of("stock", stockPosterior);
        try {
            restTemplate.exchange(productUrl + "/stock", HttpMethod.PUT, authorizedEntity(stockUpdate), Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el stock del producto en el catálogo.");
        }

        // Save Kardex entry
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockPosterior(stockPosterior);
        return kardexRepository.save(movimiento);
    }

    private HttpEntity<?> authorizedEntity(Object body) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpHeaders headers = new HttpHeaders();
        if (attributes != null) {
            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null) {
                headers.set(HttpHeaders.AUTHORIZATION, authorization);
            }
        }
        return new HttpEntity<>(body, headers);
    }
}
