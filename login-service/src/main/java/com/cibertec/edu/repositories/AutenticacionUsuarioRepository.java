package com.cibertec.edu.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.cibertec.edu.models.AutenticacionUsuario;

@Repository
public interface AutenticacionUsuarioRepository extends CrudRepository<AutenticacionUsuario, Integer> {
	Optional<AutenticacionUsuario> findByUsername(String username);
}
