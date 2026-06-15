package com.cibertec.edu.Models;

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
@Table(name = "Kardex")
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kardex")
    private Long id;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento; // ENTRADA, SALIDA, AJUSTE

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private Integer stockPosterior;

    @Column(name = "justificacion", nullable = false, length = 255)
    private String justificacion;

    @Column(name = "id_usuario", nullable = false)
    private Integer idUsuario;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
