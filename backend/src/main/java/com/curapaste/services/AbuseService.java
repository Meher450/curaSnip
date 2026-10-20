package com.curapaste.services;


import org.springframework.stereotype.Service;

@Service
public class AbuseService {
    private static final String ABUSE_MARKER = "[[ABUSE_TEST]]";
    public boolean isSuspicious(String content) {

        if (content == null) {
            return false;
        }

        return content.contains(ABUSE_MARKER);
    }
}
