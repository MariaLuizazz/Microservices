package aula.microservicos.playyourlist.musica;

import org.springframework.data.repository.CrudRepository;

public interface MusicaRepository extends CrudRepository<Musica, Integer> {
}
