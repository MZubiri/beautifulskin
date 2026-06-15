package com.cibertec.edu.Repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cibertec.edu.Models.Proveedor;

@Repository
public interface IProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRuc(String ruc);
}
