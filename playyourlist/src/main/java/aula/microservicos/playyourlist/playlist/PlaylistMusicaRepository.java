package aula.microservicos.playyourlist.playlist;

import org.springframework.data.repository.CrudRepository;

public interface PlaylistMusicaRepository extends CrudRepository<PlaylistMusica, Integer> {

    // localizar todas as associações de uma playlist
    Iterable<PlaylistMusica> findByPlaylistId(Integer playlistId);

    // remover uma música específica de uma playlist
    void deleteByPlaylistIdAndMusicaId(Integer playlistId, Integer musicaId);

    // remover todas as associações de uma playlist (usado ao excluir a playlist)
    void deleteByPlaylistId(Integer playlistId);

}
