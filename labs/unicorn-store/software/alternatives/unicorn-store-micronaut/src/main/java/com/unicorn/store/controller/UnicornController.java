package com.unicorn.store.controller;

import com.unicorn.store.model.Unicorn;
import com.unicorn.store.service.UnicornService;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class UnicornController {

    private static final Logger logger = LoggerFactory.getLogger(UnicornController.class);

    private final UnicornService unicornService;

    public UnicornController(UnicornService unicornService) {
        this.unicornService = unicornService;
    }

    @Post("/unicorns")
    public Unicorn createUnicorn(@Body Unicorn unicorn) {
        return unicornService.createUnicorn(unicorn);
    }

    @Put("/unicorns/{unicornId}")
    public Unicorn updateUnicorn(@Body Unicorn unicorn, @PathVariable String unicornId) {
        return unicornService.updateUnicorn(unicorn, unicornId);
    }

    @Get("/unicorns/{unicornId}")
    public Unicorn getUnicorn(@PathVariable String unicornId) {
        return unicornService.getUnicorn(unicornId);
    }

    @Delete("/unicorns/{unicornId}")
    public void deleteUnicorn(@PathVariable String unicornId) {
        unicornService.deleteUnicorn(unicornId);
    }
}
