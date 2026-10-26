package aula.microservicos.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastrar um novo usuario")
public record NovoUsuarioRequest(

		@Schema(description = "Nome do novo usuario", example = "maria")
		@NotBlank @Size(max = 100) String username,

		@Schema(description = "Senha (maximo 10 caracteres)", example = "abc123")
		@NotBlank @Size(max = 10) String senha) {
}
