package com.cibertec.edu.services;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cibertec.edu.dto.TokenDto;
import com.cibertec.edu.dto.UsuarioDto;
import com.cibertec.edu.models.AutenticacionUsuario;
import com.cibertec.edu.repositories.AutenticacionUsuarioRepository;
import com.cibertec.edu.security.JwtProvider;

@Service
public class AutenticacionUsuarioService {

	private final AutenticacionUsuarioRepository autRepository;
	private final PasswordEncoder encoder;
	private final JwtProvider jwtProvider;

	public AutenticacionUsuarioService(AutenticacionUsuarioRepository authRepository, 
									   PasswordEncoder encoder, 
									   JwtProvider jwtProvider) {
		super();
		this.autRepository = authRepository;
		this.encoder = encoder;
		this.jwtProvider = jwtProvider;
	}
	
	public AutenticacionUsuario create(UsuarioDto dto) {
		if (dto == null || dto.getNickname() == null || dto.getNickname().trim().isEmpty()
				|| dto.getPassword() == null || dto.getPassword().isEmpty()) {
			throw new IllegalArgumentException("El usuario y la contraseña son obligatorios.");
		}
		Optional<AutenticacionUsuario> usuario = this.autRepository.findByUsername(dto.getNickname());
		if(usuario.isPresent()) {
			return null;
		}
		String password = encoder.encode(dto.getPassword());
		AutenticacionUsuario user = AutenticacionUsuario.builder()
				.username(dto.getNickname())
				.password(password)
				.rol("ROLE_TRABAJADOR")
				.build();
		return autRepository.save(user);
	}
	
	public TokenDto login(UsuarioDto dto) {
		if (dto == null || dto.getNickname() == null || dto.getPassword() == null) {
			return null;
		}
		Optional<AutenticacionUsuario> usuario = this.autRepository.findByUsername(dto.getNickname());
		if(!usuario.isPresent()) {
			return null;
		}
		if(encoder.matches(dto.getPassword(), usuario.get().getPassword())) {
			AutenticacionUsuario user = usuario.get();
			return TokenDto.builder()
					.token(jwtProvider.createToken(user))
					.role(user.getRol())
					.username(user.getUsername())
					.build();
		}
		return null;
	}
	
	public TokenDto validate(String token) {
		if(!jwtProvider.validate(token)) {
			return null;
		}
		String username = jwtProvider.getUserNameFromToken(token);
		Optional<AutenticacionUsuario> userOpt = autRepository.findByUsername(username);
		if(!userOpt.isPresent()) {
			return null;
		}
		return TokenDto.builder()
				.token(token)
				.role(userOpt.get().getRol())
				.username(userOpt.get().getUsername())
				.build();
	}

}
