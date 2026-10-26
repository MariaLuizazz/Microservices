package aula.microservicos.login;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aula.microservicos.login.dto.UsuarioResponse;

@Service
public class LoginService {

	// "falhar mais de uma vez" = na 2a falha seguida o usuario e bloqueado
	private static final int MAX_FALHAS = 2;

	public enum ResultadoTroca {
		OK, NAO_ENCONTRADO, SENHA_ATUAL_INVALIDA
	}

	private final JdbcTemplate jdbc;
	private final UserDetailsService userDetailsService; // e o JdbcUserDetailsManager
	private final PasswordEncoder encoder;
	private final MetricasLogin metricas;

	public LoginService(JdbcTemplate jdbc, UserDetailsService userDetailsService, PasswordEncoder encoder,
			MetricasLogin metricas) {
		this.jdbc = jdbc;
		this.userDetailsService = userDetailsService;
		this.encoder = encoder;
		this.metricas = metricas;
	}

	// true = login valido (200) / false = invalido (401)
	public boolean login(String username, String senha) {

		UserDetails usuario;
		try {
			usuario = userDetailsService.loadUserByUsername(username);
		} catch (UsernameNotFoundException e) {
			metricas.loginInvalido();
			return false;
		}

		if (bloqueado(username)) {
			metricas.loginInvalido();
			return false;
		}

		if (encoder.matches(senha, usuario.getPassword())) {
			metricas.loginValido();
			jdbc.update("update login_attempts set failed_attempts = 0, updated_at = CURRENT_TIMESTAMP where username = ?",
					username);
			return true;
		}

		metricas.loginInvalido();
		registrarFalha(username);
		return false;
	}

	private void registrarFalha(String username) {

		int linhas = jdbc.update("update login_attempts set failed_attempts = failed_attempts + 1, "
				+ "last_failed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP where username = ?", username);

		if (linhas == 0) {
			jdbc.update("insert into login_attempts (username, failed_attempts, last_failed_at) "
					+ "values (?, 1, CURRENT_TIMESTAMP)", username);
		}

		jdbc.update("update login_attempts set locked = true, locked_at = CURRENT_TIMESTAMP, "
				+ "updated_at = CURRENT_TIMESTAMP where username = ? and failed_attempts >= ? and locked = false",
				username, MAX_FALHAS);
	}

	private boolean bloqueado(String username) {
		Integer total = jdbc.queryForObject(
				"select count(*) from login_attempts where username = ? and locked = true", Integer.class, username);
		return total != null && total > 0;
	}

	public boolean existe(String username) {
		Integer total = jdbc.queryForObject("select count(*) from usuarios where username = ?", Integer.class, username);
		return total != null && total > 0;
	}

	public ResultadoTroca trocarSenha(String username, String atual, String nova) {

		List<String> senhas = jdbc.queryForList("select senha from usuarios where username = ?", String.class, username);

		if (senhas.isEmpty()) {
			return ResultadoTroca.NAO_ENCONTRADO;
		}

		if (!encoder.matches(atual, senhas.get(0))) {
			return ResultadoTroca.SENHA_ATUAL_INVALIDA;
		}

		jdbc.update("update usuarios set senha = ? where username = ?", encoder.encode(nova), username);
		return ResultadoTroca.OK;
	}

	// Todo usuario novo recebe ROLE_USER (o JdbcUserDetailsManager exige ao menos uma authority)
	@Transactional
	public void criar(String username, String senha) {
		jdbc.update("insert into usuarios (username, senha) values (?, ?)", username, encoder.encode(senha));
		jdbc.update("insert into authorities (username, authority) values (?, 'ROLE_USER')", username);
	}

	// false = usuario nao existe
	public boolean desbloquear(String username) {

		if (!existe(username)) {
			return false;
		}

		jdbc.update("update login_attempts set locked = false, failed_attempts = 0, locked_at = null, "
				+ "updated_at = CURRENT_TIMESTAMP where username = ?", username);
		return true;
	}

	public List<UsuarioResponse> listar() {
		return jdbc.query("select u.username, coalesce(l.locked, false) as bloqueado, "
				+ "coalesce(l.failed_attempts, 0) as falhas "
				+ "from usuarios u left join login_attempts l on l.username = u.username order by u.username",
				(rs, i) -> new UsuarioResponse(rs.getString("username"), rs.getBoolean("bloqueado"),
						rs.getInt("falhas")));
	}

}
