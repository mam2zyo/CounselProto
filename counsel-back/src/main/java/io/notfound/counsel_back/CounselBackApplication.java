package io.notfound.counsel_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CounselBackApplication {

	public static void main(String[] args) {
		SpringApplication.run(CounselBackApplication.class, args);
	}

}
