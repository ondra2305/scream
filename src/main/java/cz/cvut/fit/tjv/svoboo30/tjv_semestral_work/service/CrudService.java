package cz.cvut.fit.tjv.svoboo30.tjv_semestral_work.service;

import java.util.Optional;

public interface CrudService<T, ID> {
    T create(T e);

    Optional<T> readById(ID id);

    Iterable<T> readAll();

    void update(ID id, T e);

    void deleteById(ID id);
}
