package aula.microservicos.login.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		http
			.authorizeHttpRequests(auth -> auth
				// publicos
				.requestMatchers(HttpMethod.POST, "/login").permitAll()
				.requestMatchers(HttpMethod.PUT, "/trocarsenha/*").permitAll()
				// somente ROLE_MANUTENCAO
				.requestMatchers(HttpMethod.POST, "/").hasRole("MANUTENCAO")
				.requestMatchers(HttpMethod.GET, "/").hasRole("MANUTENCAO")
				// somente ROLE_ADMIN
				.requestMatchers(HttpMethod.PUT, "/unlock/*").hasRole("ADMIN")
				// infraestrutura: Swagger, Actuator/Prometheus e console do H2
				.requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
				.requestMatchers("/actuator/**", "/h2-console/**").permitAll()
				.anyRequest().authenticated())
			.httpBasic(Customizer.withDefaults())
			.csrf(csrf -> csrf.disable())
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())) // o console do H2 usa frames
			.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

	// Usuarios e roles vindos do banco, nas tabelas do enunciado:
	//   usuarios(username, senha)  e  authorities(username, authority)
	// Um usuario bloqueado (login_attempts.locked = true) vem com enabled = false
	@Bean
	JdbcUserDetailsManager userDetailsManager(DataSource dataSource) {

		JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

		manager.setUsersByUsernameQuery(
				"select u.username, u.senha, not coalesce(l.locked, false) "
						+ "from usuarios u left join login_attempts l on l.username = u.username "
						+ "where u.username = ?");

		manager.setAuthoritiesByUsernameQuery(
				"select username, authority from authorities where username = ?");

		return manager;
	}

	// A coluna senha tem so 10 caracteres (enunciado), entao nao cabe um hash BCrypt (60).
	// Por isso a senha e guardada e comparada como texto puro. SO PARA ESTUDO.
	@Bean
	PasswordEncoder passwordEncoder() {
		return new PasswordEncoder() {

			@Override
			public String encode(CharSequence senha) {
				return senha.toString();
			}

			@Override
			public boolean matches(CharSequence senha, String senhaGuardada) {
				return senhaGuardada != null && senhaGuardada.contentEquals(senha);
			}
		};
	}

}
