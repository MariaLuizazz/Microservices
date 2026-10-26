package aula.microservicos.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Troca de senha: senha atual e a nova senha")
public record TrocaSenhaRequest(

		@Schema(description = "Senha atual", example = "123")
		@NotBlank String atual,

		@Schema(description = "Nova senha (maximo 10 caracteres)", example = "nova123")
		@NotBlank @Size(max = 10) String nova) {
}
