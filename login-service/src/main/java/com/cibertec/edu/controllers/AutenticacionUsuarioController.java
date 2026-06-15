package com.cibertec.edu.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cibertec.edu.dto.TokenDto;
import com.cibertec.edu.dto.UsuarioDto;
import com.cibertec.edu.dto.UsuarioResponseDto;
import com.cibertec.edu.models.AutenticacionUsuario;
import com.cibertec.edu.services.AutenticacionUsuarioService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/v1/auth")
public class AutenticacionUsuarioController {

private static final Logger LOGGER = LoggerFactory.getLogger(AutenticacionUsuarioController.class);
	
	
	private final AutenticacionUsuarioService autService;


	public AutenticacionUsuarioController(AutenticacionUsuarioService authService) {
		super();
		this.autService = authService;
	}
	
	@Transactional(rollbackOn = Exception.class)
	@PostMapping("/create")
	public ResponseEntity<UsuarioResponseDto> create(@RequestBody UsuarioDto dto){
		try {
			AutenticacionUsuario authUser = autService.create(dto);
			if(authUser==null) {
				LOGGER.warn("El usuario no fue creado con exito revise los logs");
				throw new IllegalArgumentException("Revisar el contenido del DTO");
			}
			LOGGER.info("Usuario creado con exito!: " + authUser.getUsername());
			return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponseDto.builder()
					.nickname(authUser.getUsername())
					.rol(authUser.getRol())
					.build());
		} catch (Exception e) {
			LOGGER.error("Error: {}",e.getMessage());
		}
		return ResponseEntity.badRequest().build();
	}
	
	@PostMapping("/login")
	public ResponseEntity<TokenDto> login(@RequestBody UsuarioDto usuario){
		try {
			if(usuario==null) {
				throw new IllegalArgumentException("El usuario no es correcto, revise username y contraseña");
			}
			TokenDto token = autService.login(usuario);
			if (token != null) {
				LOGGER.info("Usuario logueado y JWT emitido");
				return ResponseEntity.ok(token);
			}
			
		} catch (Exception e) {
			LOGGER.error("Error en login: {}", e.getMessage());
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}
	
}
