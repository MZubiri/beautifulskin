package com.cibertec.edu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import com.cibertec.edu.Models.DetalleOrdenCompra;
import com.cibertec.edu.Models.OrdenCompra;
import com.cibertec.edu.Repository.IDetalleOrdenCompraRepository;
import com.cibertec.edu.Repository.IOrdenCompraRepository;
import com.cibertec.edu.Service.OrdenCompraService;

class OrdenCompraServiceTests {
    private IOrdenCompraRepository ordenRepository;
    private IDetalleOrdenCompraRepository detalleRepository;
    private RestTemplate restTemplate;
    private OrdenCompraService service;

    @BeforeEach
    void setUp() {
        ordenRepository = mock(IOrdenCompraRepository.class);
        detalleRepository = mock(IDetalleOrdenCompraRepository.class);
        restTemplate = mock(RestTemplate.class);
        service = new OrdenCompraService(ordenRepository, detalleRepository, restTemplate, "http://kardex-service");
        when(ordenRepository.save(any())).thenAnswer(invocation -> {
            OrdenCompra orden = invocation.getArgument(0);
            orden.setId(1L);
            return orden;
        });
    }

    private DetalleOrdenCompra detalle(int cantidad) {
        return DetalleOrdenCompra.builder().idProducto(1L).cantidad(cantidad)
                .precioUnitario(new BigDecimal("10.00")).build();
    }

    @Test void creaOrdenValida() {
        OrdenCompra creada = service.crearOrden(OrdenCompra.builder().idProveedor(1L).build(), List.of(detalle(2)));
        assertEquals(new BigDecimal("20.00"), creada.getTotal());
        assertEquals("PENDIENTE", creada.getEstado());
    }

    @Test void rechazaDetalleInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> service.crearOrden(OrdenCompra.builder().idProveedor(1L).build(), List.of(detalle(0))));
    }

    @Test void recibeOrdenAlFinal() {
        OrdenCompra orden = OrdenCompra.builder().id(1L).estado("PENDIENTE").build();
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(detalleRepository.findByIdOrden(1L)).thenReturn(List.of(detalle(2)));
        assertEquals("RECIBIDA", service.recibirOrden(1L, 1).getEstado());
    }

    @Test void rechazaRecepcionDuplicada() {
        when(ordenRepository.findById(1L)).thenReturn(Optional.of(OrdenCompra.builder().estado("RECIBIDA").build()));
        assertThrows(IllegalArgumentException.class, () -> service.recibirOrden(1L, 1));
    }
}
