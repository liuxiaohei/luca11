package org.ld.service;

import org.springframework.stereotype.Service;

@Service
public class TokenService {
    // demo演示，在引只对长度做校验
    public boolean validate(String token) {
        return token.length() > 5;
    }
}
