package aula.microservicos.playyourlist.api;

import aula.microservicos.playyourlist.reproducao.Reproducao;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reproducao-client", url = "${app.base-url}")
public interface ReproducaoFeignClient {

    @PostMapping("/reproducao")
    Reproducao registrar(@RequestBody Reproducao reproducao);

}
