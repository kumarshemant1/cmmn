package app.flo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CmmnServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CmmnServiceApplication.class, args);
	}

}
