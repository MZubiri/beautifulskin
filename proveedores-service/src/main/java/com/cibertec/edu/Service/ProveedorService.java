package com.cibertec.edu.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.cibertec.edu.Models.Proveedor;
import com.cibertec.edu.Repository.IProveedorRepository;

@Service
public class ProveedorService {

    private final IProveedorRepository proveedorRepository;

    public ProveedorService(IProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<Proveedor> getAllProveedores() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> getProveedorById(Long id) {
        return proveedorRepository.findById(id);
    }

    public Optional<Proveedor> getProveedorByRuc(String ruc) {
        return proveedorRepository.findByRuc(ruc);
    }

    public Proveedor saveProveedor(Proveedor proveedor) {
        if (proveedor.getRuc() == null || proveedor.getRuc().length() != 11) {
            throw new IllegalArgumentException("El RUC del proveedor debe tener exactamente 11 dígitos.");
        }
        return proveedorRepository.save(proveedor);
    }

    public boolean existsById(Long id) {
        return proveedorRepository.existsById(id);
    }

    public void deleteProveedor(Long id) {
        proveedorRepository.deleteById(id);
    }
}
