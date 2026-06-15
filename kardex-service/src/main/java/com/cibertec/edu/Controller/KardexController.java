package com.cibertec.edu.Controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cibertec.edu.Models.Kardex;
import com.cibertec.edu.Service.KardexService;

@RestController
@RequestMapping("/api/kardex")
public class KardexController {

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    @GetMapping
    public ResponseEntity<List<Kardex>> obtenerTodoElKardex() {
        return new ResponseEntity<>(kardexService.getAllKardex(), HttpStatus.OK);
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<List<Kardex>> obtenerKardexPorProducto(@PathVariable Long idProducto) {
        return new ResponseEntity<>(kardexService.getKardexByProducto(idProducto), HttpStatus.OK);
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarMovimiento(@RequestBody Kardex movimiento) {
        try {
            Kardex nuevoMovimiento = kardexService.registrarMovimiento(movimiento);
            return new ResponseEntity<>(nuevoMovimiento, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
