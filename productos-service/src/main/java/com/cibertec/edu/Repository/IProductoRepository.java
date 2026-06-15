package com.cibertec.edu.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.cibertec.edu.Models.Producto;

@Repository
public interface IProductoRepository extends JpaRepository<Producto, Long> {
    java.util.Optional<Producto> findByCodigoBarras(String codigoBarras);

    @Query("SELECT p FROM Producto p WHERE p.stock <= p.stockMinimo")
    List<Producto> findLowStockProducts();
}

