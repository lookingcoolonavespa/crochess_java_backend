package com.crochess.backend.models.gameSeek;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GameSeekNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(GameSeekNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String gameSeekNotFoundHandler(GameSeekNotFoundException ex) {
        return ex.getMessage();
    }
}
