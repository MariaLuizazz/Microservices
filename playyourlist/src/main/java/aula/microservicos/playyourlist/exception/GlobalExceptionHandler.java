package aula.microservicos.playyourlist.exception;

import java.util.HashMap;
import java.util.Map;

import aula.microservicos.playyourlist.musica.MusicaNaoEncontradaException;
import aula.microservicos.playyourlist.playlist.PlaylistNaoEncontradaException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MusicaNaoEncontradaException.class)
    public ResponseEntity<String> handleMusicaNaoEncontrada(MusicaNaoEncontradaException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlaylistNaoEncontradaException.class)
    public ResponseEntity<String> handlePlaylistNaoEncontrada(PlaylistNaoEncontradaException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // erros de validacao (@Valid) - devolve um mapa campo -> mensagem
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(erro ->
                erros.put(erro.getField(), erro.getDefaultMessage()));
        return new ResponseEntity<>(erros, HttpStatus.BAD_REQUEST);
    }

    // erro 404 vindo de uma chamada Feign (endpoint /api validando playlist/musica)
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<String> handleFeignNotFound(FeignException.NotFound e) {
        return new ResponseEntity<>("Recurso não encontrado ao validar: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

}
