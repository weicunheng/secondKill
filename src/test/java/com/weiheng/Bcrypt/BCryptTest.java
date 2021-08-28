package com.weiheng.Bcrypt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@SpringBootTest(classes = BCryptTest.class)
public class BCryptTest {

    @Test
    public void hello(){

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        final String passHash = encoder.encode("123456");
        boolean matches = encoder.matches("123456", passHash);
        System.out.println(passHash);
        System.out.println(matches);
    }
}
