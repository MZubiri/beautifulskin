package com.cibertec.edu.Models;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity 
@Table(name = "Producto") 
public class Producto {
	
	   @Id 
	    @GeneratedValue(strategy = GenerationType.IDENTITY) 
	    @Column(name = "id_producto")
	    private Long id; 
	    @Column(name = "nombre", nullable = false, length = 100) 
	    private String nombre;

	    @Column(name = "descripcion", length = 200) 
	    private String descripcion;

	    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
	    private BigDecimal precio; 

	    @Column(name = "stock", nullable = false) // Mapea a 'stock', no nulo
	    private Integer stock; 
	    @Column(name = "id_categoria") 
	    private Long idCategoria; 
	    @Column(name = "stock_minimo", nullable = false)
	    private Integer stockMinimo = 5;
	    @Column(name = "codigo_barras", unique = true)
	    private String codigoBarras;
	    @Column(name = "imagen_url")
	    private String imagenUrl;
	    public Producto() {
	    }
		public Long getId() {
			return id;
		}
		public void setId(Long id) {
			this.id = id;
		}
		public String getNombre() {
			return nombre;
		}
		public void setNombre(String nombre) {
			this.nombre = nombre;
		}
		public String getDescripcion() {
			return descripcion;
		}
		public void setDescripcion(String descripcion) {
			this.descripcion = descripcion;
		}
		public BigDecimal getPrecio() {
			return precio;
		}
		public void setPrecio(BigDecimal precio) {
			this.precio = precio;
		}
		public Integer getStock() {
			return stock;
		}
		public void setStock(Integer stock) {
			this.stock = stock;
		}
		public Long getIdCategoria() {
			return idCategoria;
		}
		public void setIdCategoria(Long idCategoria) {
			this.idCategoria = idCategoria;
		}
		public Integer getStockMinimo() {
			return stockMinimo;
		}
		public void setStockMinimo(Integer stockMinimo) {
			this.stockMinimo = stockMinimo;
		}
		public String getCodigoBarras() {
			return codigoBarras;
		}
		public void setCodigoBarras(String codigoBarras) {
			this.codigoBarras = codigoBarras;
		}
		public String getImagenUrl() {
			return imagenUrl;
		}
		public void setImagenUrl(String imagenUrl) {
			this.imagenUrl = imagenUrl;
		}

}
