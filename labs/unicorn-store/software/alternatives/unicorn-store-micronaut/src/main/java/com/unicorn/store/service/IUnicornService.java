package com.unicorn.store.service;

import com.unicorn.store.model.Unicorn;

public interface IUnicornService {
    Unicorn createUnicorn(Unicorn unicorn);

    Unicorn updateUnicorn(Unicorn unicorn, String unicornId);

    Unicorn getUnicorn(String unicornId);

    void deleteUnicorn(String unicornId);
}
