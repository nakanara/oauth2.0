package com.nakanara.pactk.example.simplemvc;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class EncoderTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
        String clientSecret = "123456";

        clientSecret = encoder.encode(clientSecret);

        System.out.println(clientSecret);
    }
}
