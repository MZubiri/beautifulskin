package com.cibertec.edu.Models;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "DetalleOrdenCompra")
public class DetalleOrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @Column(name = "id_orden", nullable = false)
    private Long idOrden;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;
}
