package com.cibertec.edu.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cibertec.edu.Models.DetalleOrdenCompra;

@Repository
public interface IDetalleOrdenCompraRepository extends JpaRepository<DetalleOrdenCompra, Long> {
    List<DetalleOrdenCompra> findByIdOrden(Long idOrden);
}
