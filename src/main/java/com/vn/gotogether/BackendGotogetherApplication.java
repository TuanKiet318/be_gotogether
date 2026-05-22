package com.vn.gotogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendGotogetherApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendGotogetherApplication.class, args);
	}

}
