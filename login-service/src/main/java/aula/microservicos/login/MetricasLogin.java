package aula.microservicos.login;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

// Indicadores no Prometheus (em /actuator/prometheus):
//   login_validos_total     -> Counter: so cresce
//   login_invalidos_total   -> Counter: so cresce
//   usuarios_bloqueados     -> Gauge: sobe e desce (lido do banco a cada coleta)
@Component
public class MetricasLogin {

	private final Counter validos;
	private final Counter invalidos;

	public MetricasLogin(MeterRegistry registry, JdbcTemplate jdbc) {

		this.validos = Counter.builder("login.validos")
				.description("Quantidade de logins validos")
				.register(registry);

		this.invalidos = Counter.builder("login.invalidos")
				.description("Quantidade de logins invalidos")
				.register(registry);

		Gauge.builder("usuarios.bloqueados", jdbc, MetricasLogin::contarBloqueados)
				.description("Total de usuarios bloqueados")
				.register(registry);
	}

	public void loginValido() {
		validos.increment();
	}

	public void loginInvalido() {
		invalidos.increment();
	}

	private static double contarBloqueados(JdbcTemplate jdbc) {
		try {
			Integer total = jdbc.queryForObject("select count(*) from login_attempts where locked = true", Integer.class);
			return total == null ? 0 : total;
		} catch (DataAccessException e) {
			return 0;
		}
	}

}
