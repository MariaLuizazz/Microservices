package aula.microservicos.playyourlist.playlist;

public class PlaylistNaoEncontradaException extends RuntimeException {

    public PlaylistNaoEncontradaException(Integer id) {
        super("Playlist não encontrada com id: " + id);
    }

}
