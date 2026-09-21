package aula.microservicos.playyourlist.api;

import aula.microservicos.playyourlist.musica.Musica;
import aula.microservicos.playyourlist.playlist.Playlist;
import aula.microservicos.playyourlist.reproducao.Reproducao;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final MusicaFeignClient musicaClient;
    private final PlaylistFeignClient playlistClient;
    private final ReproducaoFeignClient reproducaoClient;

    public ApiController(MusicaFeignClient musicaClient,
                          PlaylistFeignClient playlistClient,
                          ReproducaoFeignClient reproducaoClient) {
        this.musicaClient = musicaClient;
        this.playlistClient = playlistClient;
        this.reproducaoClient = reproducaoClient;
    }

    // POST /api/adicionar/{playlistId}/musicas/{musicaId}
    // valida playlist e musica via Feign antes de associar
    @PostMapping("/adicionar/{playlistId}/musicas/{musicaId}")
    public ResponseEntity<String> adicionarMusicaAPlaylist(@PathVariable Integer playlistId,
                                                             @PathVariable Integer musicaId) {

        // se a playlist ou a musica nao existirem, o Feign recebe 404 do proprio
        // endpoint interno, o que dispara FeignException.NotFound (tratada no
        // GlobalExceptionHandler)
        Playlist playlist = playlistClient.buscarPlaylist(playlistId);
        Musica musica = musicaClient.buscarMusica(musicaId);

        playlistClient.adicionarMusica(playlistId, musicaId);

        String mensagem = "Música " + musica.getTitulo() + " adicionada com sucesso à playlist " + playlist.getNome();
        return new ResponseEntity<>(mensagem, HttpStatus.OK);
    }

    // PUT /api/executar/{playlistId}
    // executa a playlist, gerando um registro de reproducao via Feign
    @PutMapping("/executar/{playlistId}")
    public ResponseEntity<Reproducao> executarPlaylist(@PathVariable Integer playlistId) {

        // valida se a playlist existe
        playlistClient.buscarPlaylist(playlistId);

        Reproducao reproducao = new Reproducao();
        reproducao.setPlaylistId(playlistId);

        Reproducao registrada = reproducaoClient.registrar(reproducao);
        return new ResponseEntity<>(registrada, HttpStatus.OK);
    }

}
