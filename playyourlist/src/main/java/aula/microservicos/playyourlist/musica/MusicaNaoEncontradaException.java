package aula.microservicos.playyourlist.musica;

public class MusicaNaoEncontradaException extends RuntimeException {

    public MusicaNaoEncontradaException(Integer id) {
        super("Música não encontrada com id: " + id);
    }

}
