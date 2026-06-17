package com.cibertec.edu.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.cibertec.edu.Models.Producto;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrue();

    java.util.Optional<Producto> findByIdAndActivoTrue(Long id);

    java.util.Optional<Producto> findByCodigoBarrasAndActivoTrue(String codigoBarras);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND p.stock <= p.stockMinimo")
    List<Producto> findLowStockProducts();
}

