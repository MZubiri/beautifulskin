package com.cibertec.edu.dto;

import java.util.List;
import com.cibertec.edu.Models.DetalleOrdenCompra;
import com.cibertec.edu.Models.OrdenCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CrearOrdenDto {
    private OrdenCompra orden;
    private List<DetalleOrdenCompra> detalles;
}
