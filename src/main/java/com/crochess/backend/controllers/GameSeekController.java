package com.crochess.backend.controllers;
import com.crochess.backend.models.gameSeek.GameSeek;
import com.crochess.backend.daos_and_repos.GameSeekRepository;
import org.springframework.web.bind.annotation.*;

@RequestMapping("api/gameseeks")
@RestController
public class GameSeekController {
    private final GameSeekRepository repository;

    GameSeekController(GameSeekRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    Iterable<GameSeek> all() {
        return repository.findAll();
    }

    @PostMapping
    GameSeek newGameSeek(@RequestBody GameSeek newGameSeek) {
        repository.save(newGameSeek);
        return newGameSeek;
    }

    @DeleteMapping("/{id}")
    void deleteById(@PathVariable int id) {
        repository.deleteById(id);
    }


}
