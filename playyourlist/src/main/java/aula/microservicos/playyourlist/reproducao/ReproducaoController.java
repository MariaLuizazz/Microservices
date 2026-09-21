package aula.microservicos.playyourlist.reproducao;

import java.time.LocalDateTime;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reproducao")
public class ReproducaoController {

    private final ReproducaoRepository repo;

    public ReproducaoController(ReproducaoRepository repo) {
        this.repo = repo;
    }

    // POST /reproducao - cria um registro de reproducao da playlist
    // corpo esperado: { "playlistId": 1 }
    @PostMapping
    public ResponseEntity<Reproducao> registrar(@Valid @RequestBody Reproducao reproducao) {
        reproducao.setDataHora(LocalDateTime.now());
        Reproducao salva = repo.save(reproducao);
        return new ResponseEntity<>(salva, HttpStatus.CREATED);
    }

    // GET /reproducao/{playlistid} - lista todas as reproducoes de uma playlist
    @GetMapping("/{playlistid}")
    public ResponseEntity<Iterable<Reproducao>> listarPorPlaylist(@PathVariable Integer playlistid) {
        return new ResponseEntity<>(repo.findByPlaylistId(playlistid), HttpStatus.OK);
    }

    // GET /reproducao/total/{playlistid} - total de vezes que a playlist foi executada
    @GetMapping("/total/{playlistid}")
    public ResponseEntity<Long> total(@PathVariable Integer playlistid) {
        return new ResponseEntity<>(repo.countByPlaylistId(playlistid), HttpStatus.OK);
    }

}
