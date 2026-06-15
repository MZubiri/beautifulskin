package com.cibertec.edu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsuarioDto {
	private String nickname;
	private String password;
	private String rol;

}
