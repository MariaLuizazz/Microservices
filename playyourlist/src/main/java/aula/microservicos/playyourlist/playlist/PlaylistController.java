package aula.microservicos.playyourlist.playlist;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistRepository repo;
    private final PlaylistMusicaRepository playlistMusicaRepo;

    public PlaylistController(PlaylistRepository repo, PlaylistMusicaRepository playlistMusicaRepo) {
        this.repo = repo;
        this.playlistMusicaRepo = playlistMusicaRepo;
    }

    // POST /playlists - criar uma nova playlist
    @PostMapping
    public ResponseEntity<Playlist> criar(@Valid @RequestBody Playlist playlist) {
        Playlist salva = repo.save(playlist);
        return new ResponseEntity<>(salva, HttpStatus.CREATED);
    }

    // GET /playlists - listar todas as playlists
    @GetMapping
    public ResponseEntity<Iterable<Playlist>> listar() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    // GET /playlists/{playlistid} - buscar uma playlist pelo id
    @GetMapping("/{playlistid}")
    public ResponseEntity<Playlist> buscar(@PathVariable Integer playlistid) {
        Playlist playlist = repo.findById(playlistid)
                .orElseThrow(() -> new PlaylistNaoEncontradaException(playlistid));
        return new ResponseEntity<>(playlist, HttpStatus.OK);
    }

    // PUT /playlists/{playlistid} - atualizar nome e descricao
    @PutMapping("/{playlistid}")
    public ResponseEntity<Playlist> atualizar(@PathVariable Integer playlistid, @Valid @RequestBody Playlist playlist) {
        Playlist existente = repo.findById(playlistid)
                .orElseThrow(() -> new PlaylistNaoEncontradaException(playlistid));
        existente.setNome(playlist.getNome());
        existente.setDescricao(playlist.getDescricao());
        Playlist atualizada = repo.save(existente);
        return new ResponseEntity<>(atualizada, HttpStatus.OK);
    }

    // DELETE /playlists/{playlistid} - excluir a playlist e suas musicas associadas
    @DeleteMapping("/{playlistid}")
    public ResponseEntity<Void> excluir(@PathVariable Integer playlistid) {
        if (!repo.existsById(playlistid)) {
            throw new PlaylistNaoEncontradaException(playlistid);
        }
        playlistMusicaRepo.deleteByPlaylistId(playlistid);
        repo.deleteById(playlistid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // POST /playlists/{playlistid}/musicas/{musicaId} - adicionar musica a playlist
    @PostMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<PlaylistMusica> adicionarMusica(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        if (!repo.existsById(playlistid)) {
            throw new PlaylistNaoEncontradaException(playlistid);
        }
        PlaylistMusica associacao = new PlaylistMusica(playlistid, musicaId);
        PlaylistMusica salva = playlistMusicaRepo.save(associacao);
        return new ResponseEntity<>(salva, HttpStatus.CREATED);
    }

    // DELETE /playlists/{playlistid}/musicas/{musicaId} - remover musica da playlist
    @DeleteMapping("/{playlistid}/musicas/{musicaId}")
    public ResponseEntity<Void> removerMusica(@PathVariable Integer playlistid, @PathVariable Integer musicaId) {
        if (!repo.existsById(playlistid)) {
            throw new PlaylistNaoEncontradaException(playlistid);
        }
        playlistMusicaRepo.deleteByPlaylistIdAndMusicaId(playlistid, musicaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // GET /playlists/{playlistid}/musicas - listar ids das musicas da playlist
    @GetMapping("/{playlistid}/musicas")
    public ResponseEntity<List<Integer>> listarMusicas(@PathVariable Integer playlistid) {
        if (!repo.existsById(playlistid)) {
            throw new PlaylistNaoEncontradaException(playlistid);
        }
        List<Integer> musicaIds = StreamSupport
                .stream(playlistMusicaRepo.findByPlaylistId(playlistid).spliterator(), false)
                .map(PlaylistMusica::getMusicaId)
                .collect(Collectors.toList());
        return new ResponseEntity<>(musicaIds, HttpStatus.OK);
    }

}
