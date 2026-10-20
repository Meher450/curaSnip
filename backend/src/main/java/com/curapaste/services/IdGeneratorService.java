package com.curapaste.services;


import org.springframework.stereotype.Component;


import java.security.SecureRandom;

@Component
public class IdGeneratorService {
    private static final String ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int ID_LENGTH = 7;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateId(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < ID_LENGTH; i++){
            sb.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
