package com.unicorn.store.data;

import com.unicorn.store.model.Unicorn;
import com.unicorn.store.model.UnicornEventType;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class UnicornRepository {

    public Optional<Unicorn> findById(String unicornId) {
        Unicorn entity = Unicorn.findById(unicornId);
        return Optional.of(entity);
    }

    @Transactional
    public Unicorn save(Unicorn unicorn) {
        unicorn.persist();
        return unicorn;
    }

    @Transactional
    public Unicorn update(Unicorn unicorn, String unicornId) {
        Unicorn savedUnicorn = Unicorn.findById(unicornId);
        savedUnicorn.setName(unicorn.getName());
        savedUnicorn.setSize(unicorn.getSize());
        savedUnicorn.setType(unicorn.getType());
        savedUnicorn.setAge(unicorn.getAge());
        savedUnicorn.persist();
        return savedUnicorn;
    }

    @Transactional
    public void delete(Unicorn unicorn) {
        var entity = Unicorn.findById(unicorn.getId());
        entity.delete();
    }
}
