package com.cibertec.edu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cibertec.edu.Models.Producto;
import com.cibertec.edu.Repository.IProductoRepository;
import com.cibertec.edu.Service.ProductoService;

class ProductoServiceTests {
    private IProductoRepository repository;
    private ProductoService service;

    @BeforeEach
    void setUp() {
        repository = mock(IProductoRepository.class);
        service = new ProductoService(repository);
    }

    private Producto producto(int stock) {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Bloqueador");
        p.setPrecio(new BigDecimal("20.00"));
        p.setStock(stock);
        p.setStockMinimo(5);
        return p;
    }

    @Test void listaProductos() {
        when(repository.findByActivoTrue()).thenReturn(List.of(producto(10)));
        assertEquals(1, service.getAllProductos().size());
    }

    @Test void buscaProducto() {
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(producto(10)));
        assertTrue(service.getProductoById(1L).isPresent());
    }

    @Test void creaProductoValido() {
        Producto p = producto(10);
        when(repository.save(p)).thenReturn(p);
        assertSame(p, service.saveProducto(p));
    }

    @Test void actualizaProductoSinCambiarStock() {
        Producto existente = producto(25);
        Producto cambios = producto(999);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(existente));
        when(repository.save(cambios)).thenReturn(cambios);
        assertEquals(25, service.updateProducto(1L, cambios).getStock());
    }

    @Test void eliminaProducto() {
        Producto p = producto(10);
        when(repository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(p));
        service.deleteProducto(1L);
        assertFalse(p.getActivo());
        verify(repository).save(p);
    }

    @Test void rechazaPrecioInvalido() {
        Producto p = producto(10);
        p.setPrecio(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> service.saveProducto(p));
    }
}
