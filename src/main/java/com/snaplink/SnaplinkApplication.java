package com.snaplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Điểm khởi động backend. Spring Boot quét các class trong com.snaplink để tạo và nối các bean. */
@SpringBootApplication
public class SnaplinkApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnaplinkApplication.class, args);
	}

}
