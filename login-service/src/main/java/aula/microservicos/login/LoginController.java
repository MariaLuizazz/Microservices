package aula.microservicos.login;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import aula.microservicos.login.dto.LoginRequest;
import aula.microservicos.login.dto.Mensagem;
import aula.microservicos.login.dto.NovoUsuarioRequest;
import aula.microservicos.login.dto.TrocaSenhaRequest;
import aula.microservicos.login.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@Tag(name = "Usuarios e login", description = "Login, troca de senha, cadastro, desbloqueio e listagem de usuarios")
public class LoginController {

	private final LoginService service;

	public LoginController(LoginService service) {
		this.service = service;
	}

	@Operation(summary = "Efetua login", description = "Publico. Valida usuario e senha na tabela usuarios. "
			+ "Na segunda falha seguida o usuario e bloqueado.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuario e senha validos"),
			@ApiResponse(responseCode = "401", description = "Usuario e senha invalidos (ou usuario bloqueado)", content = @Content) })
	@PostMapping("/login")
	public ResponseEntity<Mensagem> login(@Valid @RequestBody LoginRequest request) {

		if (service.login(request.username(), request.senha())) {
			return ResponseEntity.ok(new Mensagem("Login efetuado com sucesso"));
		}

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Mensagem("Usuario ou senha invalidos"));
	}

	@Operation(summary = "Troca a senha", description = "Publico. Exige a senha atual correta.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Senha alterada"),
			@ApiResponse(responseCode = "400", description = "Corpo invalido (nova senha com mais de 10 caracteres)", content = @Content),
			@ApiResponse(responseCode = "401", description = "Senha atual incorreta", content = @Content),
			@ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content) })
	@PutMapping("/trocarsenha/{userid}")
	public ResponseEntity<Mensagem> trocarSenha(
			@Parameter(description = "Nome do usuario", example = "joao") @PathVariable String userid,
			@Valid @RequestBody TrocaSenhaRequest request) {

		switch (service.trocarSenha(userid, request.atual(), request.nova())) {
		case NAO_ENCONTRADO:
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
		case SENHA_ATUAL_INVALIDA:
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta");
		default:
			return ResponseEntity.ok(new Mensagem("Senha alterada com sucesso"));
		}
	}

	@Operation(summary = "Cadastra usuario", description = "Somente ROLE_MANUTENCAO. O novo usuario recebe ROLE_USER.",
			security = @SecurityRequirement(name = "basicAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Usuario criado"),
			@ApiResponse(responseCode = "400", description = "Corpo invalido", content = @Content),
			@ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
			@ApiResponse(responseCode = "409", description = "Usuario ja existe", content = @Content) })
	@PostMapping("/")
	public ResponseEntity<Mensagem> cadastrar(@Valid @RequestBody NovoUsuarioRequest request) {

		if (service.existe(request.username())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario ja existe");
		}

		service.criar(request.username(), request.senha());
		return ResponseEntity.status(HttpStatus.CREATED).body(new Mensagem("Usuario criado com sucesso"));
	}

	@Operation(summary = "Desbloqueia usuario", description = "Somente ROLE_ADMIN.",
			security = @SecurityRequirement(name = "basicAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Usuario desbloqueado"),
			@ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
			@ApiResponse(responseCode = "404", description = "Usuario nao encontrado", content = @Content) })
	@PutMapping("/unlock/{userid}")
	public ResponseEntity<Mensagem> desbloquear(
			@Parameter(description = "Nome do usuario a desbloquear", example = "joao") @PathVariable String userid) {

		if (!service.desbloquear(userid)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado");
		}

		return ResponseEntity.ok(new Mensagem("Usuario desbloqueado"));
	}

	@Operation(summary = "Lista usuarios", description = "Somente ROLE_MANUTENCAO. A senha nao e devolvida.",
			security = @SecurityRequirement(name = "basicAuth"))
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Lista de usuarios"),
			@ApiResponse(responseCode = "401", description = "Nao autenticado", content = @Content),
			@ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content) })
	@GetMapping("/")
	public List<UsuarioResponse> listar() {
		return service.listar();
	}

}
