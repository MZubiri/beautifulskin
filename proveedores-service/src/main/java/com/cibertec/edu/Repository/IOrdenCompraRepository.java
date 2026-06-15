package com.cibertec.edu.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cibertec.edu.Models.OrdenCompra;

@Repository
public interface IOrdenCompraRepository extends JpaRepository<OrdenCompra, Long> {
    List<OrdenCompra> findByIdProveedor(Long idProveedor);
    List<OrdenCompra> findAllByOrderByFechaCreacionDesc();
}
