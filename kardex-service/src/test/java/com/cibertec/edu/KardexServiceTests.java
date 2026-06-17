package com.cibertec.edu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.cibertec.edu.Models.Kardex;
import com.cibertec.edu.Repository.IKardexRepository;
import com.cibertec.edu.Service.KardexService;

class KardexServiceTests {
    private IKardexRepository repository;
    private RestTemplate restTemplate;
    private JdbcTemplate jdbcTemplate;
    private KardexService service;

    @BeforeEach
    void setUp() {
        repository = mock(IKardexRepository.class);
        restTemplate = mock(RestTemplate.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        service = new KardexService(repository, restTemplate, jdbcTemplate, "http://productos-service");
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM authusuario WHERE id = ?"), eq(Integer.class), eq(1)))
                .thenReturn(1);
        when(restTemplate.exchange(contains("/api/productos/1"), eq(org.springframework.http.HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("stock", 10)));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Kardex movimiento(String tipo, int cantidad) {
        return Kardex.builder().idProducto(1L).tipoMovimiento(tipo).cantidad(cantidad)
                .justificacion("Prueba").idUsuario(1).build();
    }

    @Test void registraEntrada() {
        assertEquals(15, service.registrarMovimiento(movimiento("ENTRADA", 5)).getStockPosterior());
    }

    @Test void registraSalida() {
        assertEquals(7, service.registrarMovimiento(movimiento("SALIDA", 3)).getStockPosterior());
    }

    @Test void registraAjuste() {
        assertEquals(4, service.registrarMovimiento(movimiento("AJUSTE", 4)).getStockPosterior());
    }

    @Test void rechazaCantidadInvalida() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarMovimiento(movimiento("ENTRADA", 0)));
    }

    @Test void rechazaStockInsuficiente() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarMovimiento(movimiento("SALIDA", 11)));
    }
}
