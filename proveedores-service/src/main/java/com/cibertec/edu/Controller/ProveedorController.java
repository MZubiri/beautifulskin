package com.cibertec.edu.Controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cibertec.edu.Models.Proveedor;
import com.cibertec.edu.Service.ProveedorService;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public ResponseEntity<List<Proveedor>> obtenerTodosLosProveedores() {
        return new ResponseEntity<>(proveedorService.getAllProveedores(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> obtenerProveedorPorId(@PathVariable Long id) {
        return proveedorService.getProveedorById(id)
                .map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<?> crearProveedor(@RequestBody Proveedor proveedor) {
        try {
            Proveedor nuevoProveedor = proveedorService.saveProveedor(proveedor);
            return new ResponseEntity<>(nuevoProveedor, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProveedor(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        if (!proveedorService.existsById(id)) {
            return new ResponseEntity<>("Proveedor no encontrado", HttpStatus.NOT_FOUND);
        }
        try {
            proveedor.setId(id);
            Proveedor proveedorActualizado = proveedorService.saveProveedor(proveedor);
            return new ResponseEntity<>(proveedorActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProveedor(@PathVariable Long id) {
        if (!proveedorService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        proveedorService.deleteProveedor(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
