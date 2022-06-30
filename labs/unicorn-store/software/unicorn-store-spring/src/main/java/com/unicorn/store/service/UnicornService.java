package com.unicorn.store.service;

import com.unicorn.store.data.UnicornPublisher;
import com.unicorn.store.data.UnicornRepository;
import com.unicorn.store.exceptions.ResourceNotFoundException;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.model.UnicornEventType;
import org.springframework.stereotype.Service;

@Service
public class UnicornService {
    private final UnicornRepository unicornRepository;
    private final UnicornPublisher unicornPublisher;

    public UnicornService(UnicornRepository unicornRepository, UnicornPublisher unicornPublisher) {
        this.unicornRepository = unicornRepository;
        this.unicornPublisher = unicornPublisher;
    }

    public Unicorn createUnicorn(Unicorn unicorn) {
        var savedUnicorn = unicornRepository.save(unicorn);
        unicornPublisher.publish(savedUnicorn, UnicornEventType.UNICORN_CREATED);
        return savedUnicorn;
    }

    public Unicorn updateUnicorn(Unicorn unicorn, String unicornId) {
        unicorn.setId(unicornId);
        var savedUnicorn = unicornRepository.save(unicorn);
        unicornPublisher.publish(savedUnicorn, UnicornEventType.UNICORN_UPDATED);
        return savedUnicorn;
    }

    public Unicorn getUnicorn(String unicornId) {
        var unicorn = unicornRepository.findById(unicornId);
        return unicorn.orElseThrow(ResourceNotFoundException::new);
    }

    public void deleteUnicorn(String unicornId) {
        Unicorn unicorn = unicornRepository.findById(unicornId).orElseThrow(ResourceNotFoundException::new);
        unicornRepository.delete(unicorn);
        unicornPublisher.publish(unicorn, UnicornEventType.UNICORN_DELETED);
    }
}
