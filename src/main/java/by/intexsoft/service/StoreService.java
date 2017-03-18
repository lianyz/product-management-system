package by.intexsoft.service;

import by.intexsoft.model.Store;

import java.util.List;

public interface StoreService {

    Store create(Store object);

    Store find(Integer id);

    Store findByName(String name);

    List<Store> findAll();

    Store update(Store object);

    void delete(Integer id);
}
