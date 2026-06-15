package com.cibertec.edu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cibertec.edu.dto.UsuarioDto;
import com.cibertec.edu.models.AutenticacionUsuario;
import com.cibertec.edu.repositories.AutenticacionUsuarioRepository;
import com.cibertec.edu.security.JwtProvider;
import com.cibertec.edu.services.AutenticacionUsuarioService;

class AutenticacionUsuarioServiceTests {
    private AutenticacionUsuarioRepository repository;
    private PasswordEncoder encoder;
    private JwtProvider jwtProvider;
    private AutenticacionUsuarioService service;

    @BeforeEach
    void setUp() {
        repository = mock(AutenticacionUsuarioRepository.class);
        encoder = mock(PasswordEncoder.class);
        jwtProvider = mock(JwtProvider.class);
        service = new AutenticacionUsuarioService(repository, encoder, jwtProvider);
    }

    @Test
    void creaUsuarioComoTrabajadorYConPasswordCifrado() {
        UsuarioDto dto = UsuarioDto.builder().nickname("nuevo").password("clave").rol("ROLE_ADMIN").build();
        when(repository.findByUsername("nuevo")).thenReturn(Optional.empty());
        when(encoder.encode("clave")).thenReturn("hash");
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AutenticacionUsuario creado = service.create(dto);

        assertEquals("ROLE_TRABAJADOR", creado.getRol());
        assertEquals("hash", creado.getPassword());
    }

    @Test
    void loginCorrectoDevuelveJwt() {
        AutenticacionUsuario user = AutenticacionUsuario.builder().username("Admin").password("hash").rol("ROLE_ADMIN").build();
        when(repository.findByUsername("Admin")).thenReturn(Optional.of(user));
        when(encoder.matches("admin", "hash")).thenReturn(true);
        when(jwtProvider.createToken(user)).thenReturn("jwt");

        assertEquals("jwt", service.login(UsuarioDto.builder().nickname("Admin").password("admin").build()).getToken());
    }

    @Test
    void loginIncorrectoNoDevuelveToken() {
        AutenticacionUsuario user = AutenticacionUsuario.builder().username("Admin").password("hash").build();
        when(repository.findByUsername("Admin")).thenReturn(Optional.of(user));
        when(encoder.matches("mala", "hash")).thenReturn(false);
        assertNull(service.login(UsuarioDto.builder().nickname("Admin").password("mala").build()));
    }

    @Test
    void usuarioInexistenteNoDevuelveToken() {
        when(repository.findByUsername("nadie")).thenReturn(Optional.empty());
        assertNull(service.login(UsuarioDto.builder().nickname("nadie").password("clave").build()));
    }
}
