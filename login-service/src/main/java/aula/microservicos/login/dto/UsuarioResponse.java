package aula.microservicos.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario cadastrado (a senha nunca e devolvida)")
public record UsuarioResponse(

		@Schema(description = "Nome do usuario", example = "joao") String username,

		@Schema(description = "Se o usuario esta bloqueado", example = "false") boolean bloqueado,

		@Schema(description = "Falhas de login consecutivas", example = "0") int falhas) {
}
