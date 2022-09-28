package com.unicorn.store.data;

import com.unicorn.store.model.Unicorn;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnicornRepository extends CrudRepository<Unicorn, String > {
}
