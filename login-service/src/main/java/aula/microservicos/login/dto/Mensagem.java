package aula.microservicos.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta simples com uma mensagem")
public record Mensagem(

		@Schema(example = "Login efetuado com sucesso") String mensagem) {
}
