package aula.microservicos.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login")
public record LoginRequest(

		@Schema(description = "Nome do usuario", example = "joao")
		@NotBlank String username,

		@Schema(description = "Senha do usuario", example = "123")
		@NotBlank String senha) {
}
