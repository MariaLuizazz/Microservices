package aula.microservicos.playyourlist.musica;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/musicas")
public class MusicaController {

    private final MusicaRepository repo;

    public MusicaController(MusicaRepository repo) {
        this.repo = repo;
    }

    // POST /musicas - cadastrar uma nova musica
    @PostMapping
    public ResponseEntity<Musica> cadastrar(@Valid @RequestBody Musica musica) {
        Musica salva = repo.save(musica);
        return new ResponseEntity<>(salva, HttpStatus.CREATED);
    }

    // GET /musicas - listar todas as musicas
    @GetMapping
    public ResponseEntity<Iterable<Musica>> listar() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    // GET /musicas/{id} - buscar uma musica pelo id
    @GetMapping("/{id}")
    public ResponseEntity<Musica> buscar(@PathVariable Integer id) {
        Musica musica = repo.findById(id)
                .orElseThrow(() -> new MusicaNaoEncontradaException(id));
        return new ResponseEntity<>(musica, HttpStatus.OK);
    }

    // PUT /musicas/{id} - atualizar uma musica
    @PutMapping("/{id}")
    public ResponseEntity<Musica> atualizar(@PathVariable Integer id, @Valid @RequestBody Musica musica) {
        if (!repo.existsById(id)) {
            throw new MusicaNaoEncontradaException(id);
        }
        musica.setId(id);
        Musica atualizada = repo.save(musica);
        return new ResponseEntity<>(atualizada, HttpStatus.OK);
    }

    // DELETE /musicas/{id} - excluir uma musica
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        if (!repo.existsById(id)) {
            throw new MusicaNaoEncontradaException(id);
        }
        repo.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
