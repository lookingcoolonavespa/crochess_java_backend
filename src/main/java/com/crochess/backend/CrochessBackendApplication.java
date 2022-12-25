package com.crochess.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class CrochessBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CrochessBackendApplication.class, args);
	}

}
