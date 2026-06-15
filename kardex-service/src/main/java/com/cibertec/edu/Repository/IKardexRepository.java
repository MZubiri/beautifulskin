package com.cibertec.edu.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.cibertec.edu.Models.Kardex;

@Repository
public interface IKardexRepository extends JpaRepository<Kardex, Long> {
    List<Kardex> findByIdProductoOrderByFechaDesc(Long idProducto);
    List<Kardex> findAllByOrderByFechaDesc();
}
