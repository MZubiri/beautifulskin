package com.cibertec.edu.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.transaction.Transactional;

import com.cibertec.edu.Models.DetalleOrdenCompra;
import com.cibertec.edu.Models.OrdenCompra;
import com.cibertec.edu.Repository.IDetalleOrdenCompraRepository;
import com.cibertec.edu.Repository.IOrdenCompraRepository;

@Service
public class OrdenCompraService {

    private final IOrdenCompraRepository ordenCompraRepository;
    private final IDetalleOrdenCompraRepository detalleOrdenCompraRepository;
    private final RestTemplate restTemplate;
    private final String kardexServiceUri;

    public OrdenCompraService(IOrdenCompraRepository ordenCompraRepository,
                              IDetalleOrdenCompraRepository detalleOrdenCompraRepository,
                              RestTemplate restTemplate,
                              @Value("${KARDEX_SERVICE_URI:${app.services.kardex-uri:http://localhost:8082}}") String kardexServiceUri) {
        this.ordenCompraRepository = ordenCompraRepository;
        this.detalleOrdenCompraRepository = detalleOrdenCompraRepository;
        this.restTemplate = restTemplate;
        this.kardexServiceUri = kardexServiceUri;
    }

    public List<OrdenCompra> getAllOrdenes() {
        return ordenCompraRepository.findAllByOrderByFechaCreacionDesc();
    }

    public Optional<OrdenCompra> getOrdenById(Long id) {
        return ordenCompraRepository.findById(id);
    }

    public List<DetalleOrdenCompra> getDetallesByOrden(Long idOrden) {
        return detalleOrdenCompraRepository.findByIdOrden(idOrden);
    }

    @Transactional
    public OrdenCompra crearOrden(OrdenCompra orden, List<DetalleOrdenCompra> detalles) {
        if (orden == null || orden.getIdProveedor() == null) {
            throw new IllegalArgumentException("La orden debe indicar un proveedor.");
        }
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La orden de compra debe tener al menos un producto.");
        }

        orden.setEstado("PENDIENTE");
        orden.setFechaCreacion(LocalDateTime.now());
        
        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrdenCompra d : detalles) {
            if (d.getIdProducto() == null || d.getCantidad() == null || d.getCantidad() <= 0
                    || d.getPrecioUnitario() == null || d.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Cada detalle debe tener producto, cantidad y precio positivos.");
            }
            BigDecimal subtotal = d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad()));
            total = total.add(subtotal);
        }
        orden.setTotal(total);

        OrdenCompra savedOrden = ordenCompraRepository.save(orden);

        // Save details
        for (DetalleOrdenCompra d : detalles) {
            d.setIdOrden(savedOrden.getId());
            detalleOrdenCompraRepository.save(d);
        }

        return savedOrden;
    }

    @Transactional
    public OrdenCompra recibirOrden(Long idOrden, Integer idUsuario) {
        OrdenCompra orden = ordenCompraRepository.findById(idOrden)
                .orElseThrow(() -> new IllegalArgumentException("La orden de compra no existe."));

        if ("RECIBIDA".equals(orden.getEstado())) {
            throw new IllegalArgumentException("La orden de compra ya fue recibida anteriormente.");
        }

        // Fetch details
        List<DetalleOrdenCompra> detalles = detalleOrdenCompraRepository.findByIdOrden(idOrden);
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("La orden no tiene detalles para recibir.");
        }
        for (DetalleOrdenCompra detalle : detalles) {
            // Register entry movement in kardex-service
            Map<String, Object> movement = new HashMap<>();
            movement.put("idProducto", detalle.getIdProducto());
            movement.put("tipoMovimiento", "ENTRADA");
            movement.put("cantidad", detalle.getCantidad());
            movement.put("justificacion", "Reabastecimiento: Ingreso por Orden de Compra N° " + idOrden);
            if (idUsuario != null) {
                movement.put("idUsuario", idUsuario);
            }

            try {
                restTemplate.postForObject(kardexServiceUri + "/api/kardex/registrar", authorizedEntity(movement), Map.class);
            } catch (HttpStatusCodeException e) {
                String response = e.getResponseBodyAsString();
                String detail = response == null || response.isBlank() ? e.getStatusCode().toString() : response;
                throw new IllegalArgumentException("No se pudo actualizar el kárdex para el producto "
                        + detalle.getIdProducto() + ": " + detail);
            } catch (ResourceAccessException e) {
                throw new IllegalArgumentException("No se pudo conectar con el servicio de kárdex. Intente nuevamente en unos segundos.");
            } catch (Exception e) {
                throw new IllegalArgumentException("No se pudo actualizar el kárdex para el producto "
                        + detalle.getIdProducto() + ". Intente nuevamente en unos segundos.");
            }
        }

        orden.setEstado("RECIBIDA");
        orden.setFechaRecepcion(LocalDateTime.now());
        return ordenCompraRepository.save(orden);
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
