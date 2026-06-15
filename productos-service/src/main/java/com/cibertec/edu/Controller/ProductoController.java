package com.cibertec.edu.Controller;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.cibertec.edu.Models.Producto;
import com.cibertec.edu.Service.ProductImageService;
import com.cibertec.edu.Service.ProductoService;


@RestController
@RequestMapping("/api/productos")

public class ProductoController {
	
	
    private final ProductoService productoService;
    private final ProductImageService productImageService;

     @Autowired
    public ProductoController(ProductoService productoService, ProductImageService productImageService) {
        this.productoService = productoService;
        this.productImageService = productImageService;
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody Producto producto) {
        try {
            return new ResponseEntity<>(productoService.saveProducto(producto), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodosLosProductos() {
        List<Producto> productos = productoService.getAllProductos();
        return new ResponseEntity<>(productos, HttpStatus.OK); // Retorna 200 OK
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProductoPorId(@PathVariable Long id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/barras/{codigoBarras}")
    public ResponseEntity<Producto> obtenerProductoPorCodigoBarras(@PathVariable String codigoBarras) {
        Optional<Producto> producto = productoService.getProductoByCodigoBarras(codigoBarras);
        return producto.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<Producto>> obtenerProductosAlertaStock() {
        List<Producto> productos = productoService.getLowStockProducts();
        return new ResponseEntity<>(productos, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        if (!productoService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        try {
            return new ResponseEntity<>(productoService.updateProducto(id, producto), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subirImagen(@RequestParam("file") MultipartFile file) {
        try {
            String filename = productImageService.save(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("url", "/api/productos/imagenes/" + filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/imagenes/{filename:.+}")
    public ResponseEntity<Resource> obtenerImagen(@PathVariable String filename) {
        try {
            Resource image = productImageService.load(filename);
            MediaType mediaType = MediaTypeFactory.getMediaType(filename)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return ResponseEntity.ok().contentType(mediaType).body(image);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<?> actualizarStock(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            return ResponseEntity.ok(productoService.updateStock(id, body.get("stock")));
        } catch (IllegalArgumentException e) {
            HttpStatus status = productoService.existsById(id) ? HttpStatus.BAD_REQUEST : HttpStatus.NOT_FOUND;
            return new ResponseEntity<>(e.getMessage(), status);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (!productoService.existsById(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        productoService.deleteProducto(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
