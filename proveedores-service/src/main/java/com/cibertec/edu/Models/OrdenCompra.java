package com.cibertec.edu.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Table(name = "OrdenCompra")
public class OrdenCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long id;

    @Column(name = "id_proveedor", nullable = false)
    private Long idProveedor;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE"; // PENDIENTE, RECIBIDA

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
}
