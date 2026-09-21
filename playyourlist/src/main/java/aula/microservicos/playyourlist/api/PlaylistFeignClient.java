package aula.microservicos.playyourlist.api;

import aula.microservicos.playyourlist.playlist.Playlist;
import aula.microservicos.playyourlist.playlist.PlaylistMusica;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "playlists-client", url = "${app.base-url}")
public interface PlaylistFeignClient {

    @GetMapping("/playlists/{playlistid}")
    Playlist buscarPlaylist(@PathVariable("playlistid") Integer playlistid);

    @PostMapping("/playlists/{playlistid}/musicas/{musicaId}")
    PlaylistMusica adicionarMusica(@PathVariable("playlistid") Integer playlistid,
                                    @PathVariable("musicaId") Integer musicaId);

}
