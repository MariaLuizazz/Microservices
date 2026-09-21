package aula.microservicos.playyourlist.api;

import aula.microservicos.playyourlist.musica.Musica;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "musicas-client", url = "${app.base-url}")
public interface MusicaFeignClient {

    @GetMapping("/musicas/{id}")
    Musica buscarMusica(@PathVariable("id") Integer id);

}
