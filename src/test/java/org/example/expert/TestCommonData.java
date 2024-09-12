package org.example.expert;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;

public class TestCommonData {

    public static final AuthUser AUTH_USER = new AuthUser(1L , "test@test.com" , UserRole.ADMIN);
    public static String TOKEN;

    public static void initToken(JwtUtil jwtUtil) {
        TOKEN = jwtUtil.createToken(1L , "test@test.com" , UserRole.ADMIN);
    }
}
