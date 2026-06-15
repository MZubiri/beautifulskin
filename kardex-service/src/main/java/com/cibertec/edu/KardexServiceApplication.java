package com.cibertec.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class KardexServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KardexServiceApplication.class, args);
	}

	@org.springframework.cloud.client.loadbalancer.LoadBalanced
	@org.springframework.context.annotation.Bean
	public org.springframework.web.client.RestTemplate restTemplate() {
		return new org.springframework.web.client.RestTemplate();
	}
}
