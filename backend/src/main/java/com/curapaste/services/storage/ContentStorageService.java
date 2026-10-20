package com.curapaste.services.storage;

public interface ContentStorageService {
    String store(String shortId, String content); //returns location
    String fetch(String location);

    void delete(String location);
}
