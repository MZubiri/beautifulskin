package com.cibertec.edu.Controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cibertec.edu.Models.DetalleOrdenCompra;
import com.cibertec.edu.Models.OrdenCompra;
import com.cibertec.edu.Service.OrdenCompraService;
import com.cibertec.edu.dto.CrearOrdenDto;

@RestController
@RequestMapping("/api/proveedores/ordenes")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    public OrdenCompraController(OrdenCompraService ordenCompraService) {
        this.ordenCompraService = ordenCompraService;
    }

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> obtenerTodasLasOrdenes() {
        return new ResponseEntity<>(ordenCompraService.getAllOrdenes(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> obtenerOrdenPorId(@PathVariable Long id) {
        return ordenCompraService.getOrdenById(id)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleOrdenCompra>> obtenerDetallesDeOrden(@PathVariable Long id) {
        return new ResponseEntity<>(ordenCompraService.getDetallesByOrden(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> crearOrdenCompra(@RequestBody CrearOrdenDto dto) {
        try {
            OrdenCompra nuevaOrden = ordenCompraService.crearOrden(dto.getOrden(), dto.getDetalles());
            return new ResponseEntity<>(nuevaOrden, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{id}/recibir")
    public ResponseEntity<?> recibirOrdenCompra(@PathVariable Long id, @RequestParam(required = false) Integer idUsuario) {
        try {
            OrdenCompra ordenRecibida = ordenCompraService.recibirOrden(id, idUsuario);
            return new ResponseEntity<>(ordenRecibida, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
