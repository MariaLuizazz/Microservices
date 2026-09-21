package aula.microservicos.playyourlist.reproducao;

import org.springframework.data.repository.CrudRepository;

public interface ReproducaoRepository extends CrudRepository<Reproducao, Integer> {

    // listar todas as reproducoes de uma playlist
    Iterable<Reproducao> findByPlaylistId(Integer playlistId);

    // contar quantas vezes a playlist foi reproduzida
    long countByPlaylistId(Integer playlistId);

}
