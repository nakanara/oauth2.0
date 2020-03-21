package com.nakanara.pactk.example.simplemvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
public class SimplemvcApplication {

	public static void main(String[] args) {

		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
		String clientSecret = "123456";

		clientSecret = encoder.encode(clientSecret);

		System.out.println(clientSecret);

		SpringApplication.run(SimplemvcApplication.class, args);
	}

}
