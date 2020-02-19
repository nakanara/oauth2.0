package com.nakanara.pactk.example.simplemvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@SpringBootApplication
public class SimplemvcApplication {

	@Autowired
	private Facebook facebook;

	@Autowired
	private ConnectionRepository connectionRepository;

	public static void main(String[] args) {
		SpringApplication.run(SimplemvcApplication.class, args);
	}

	@GetMapping("/")
	public String client(){
		return "client";
	}

	@GetMapping("/callback")
	public String callback(){
		return "callback_page";
	}

	@GetMapping("/message")
	public ResponseEntity<String> getMessage(){
		return ResponseEntity.ok("Hello!");
	}


	@GetMapping("/friends")
	public String friends(Model model){
		if(connectionRepository.findPrimaryConnection(Facebook.class) == null) {
			return "redirect:/connect/facebook";
		}
		return "friends";
	}
}
