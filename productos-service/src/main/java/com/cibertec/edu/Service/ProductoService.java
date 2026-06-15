package com.cibertec.edu.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cibertec.edu.Models.Producto;
import com.cibertec.edu.Repository.IProductoRepository;

@Service
public class ProductoService {
	
	private final IProductoRepository productoRepository;

	public ProductoService(IProductoRepository productoRepository) {
		this.productoRepository = productoRepository;
	}

	public List<Producto> getAllProductos() {
		return productoRepository.findAll();
	}
  
	public Optional<Producto> getProductoById(Long id) {
		return productoRepository.findById(id);
	} 
  
	public Producto saveProducto(Producto producto) {
		if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del producto es obligatorio.");
		}
		if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("El precio del producto debe ser positivo.");
		}
		if (producto.getStock() == null || producto.getStock() < 0) {
			throw new IllegalArgumentException("El stock del producto no puede ser negativo.");
		}
		if (producto.getStockMinimo() == null || producto.getStockMinimo() < 0) {
			throw new IllegalArgumentException("El stock mínimo del producto no puede ser negativo.");
		}

		return productoRepository.save(producto);
	}

	public Producto updateProducto(Long id, Producto producto) {
		Producto existente = productoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
		producto.setId(id);
		producto.setStock(existente.getStock());
		return saveProducto(producto);
	}

	public Producto updateStock(Long id, Integer stock) {
		if (stock == null || stock < 0) {
			throw new IllegalArgumentException("El stock no puede ser negativo.");
		}
		Producto existente = productoRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("El producto no existe."));
		existente.setStock(stock);
		return productoRepository.save(existente);
	}

	public boolean existsById(Long id) {
		return productoRepository.existsById(id);
	}

	public void deleteProducto(Long id) {
		productoRepository.deleteById(id);
	}

	public Optional<Producto> getProductoByCodigoBarras(String codigoBarras) {
		return productoRepository.findByCodigoBarras(codigoBarras);
	}

	public List<Producto> getLowStockProducts() {
		return productoRepository.findLowStockProducts();
	}

	@org.springframework.scheduling.annotation.Scheduled(fixedRate = 60000)
	public void alertLowStock() {
		List<Producto> lowStock = getLowStockProducts();
		if (!lowStock.isEmpty()) {
			System.out.println("=== [ALERTA DE STOCK CRÍTICO] ===");
			for (Producto p : lowStock) {
				System.out.printf("- %s: Stock actual %d (Mínimo %d)%n", p.getNombre(), p.getStock(), p.getStockMinimo());
			}
			System.out.println("=================================");
		}
	}
}
