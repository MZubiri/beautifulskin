package com.cibertec.edu.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.cibertec.edu.Models.Producto;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p WHERE p.activo = true OR p.activo IS NULL")
    List<Producto> findVisibleProducts();

    @Query("SELECT p FROM Producto p WHERE p.id = :id AND (p.activo = true OR p.activo IS NULL)")
    java.util.Optional<Producto> findVisibleById(@Param("id") Long id);

    @Query("SELECT p FROM Producto p WHERE p.codigoBarras = :codigoBarras AND (p.activo = true OR p.activo IS NULL)")
    java.util.Optional<Producto> findVisibleByCodigoBarras(@Param("codigoBarras") String codigoBarras);

    @Query("SELECT p FROM Producto p WHERE (p.activo = true OR p.activo IS NULL) AND p.stock <= p.stockMinimo")
    List<Producto> findLowStockProducts();
}

