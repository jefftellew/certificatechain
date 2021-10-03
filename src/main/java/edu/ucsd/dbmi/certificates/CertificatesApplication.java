package edu.ucsd.dbmi.certificates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@SpringBootApplication
public class CertificatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertificatesApplication.class, args);
	}

}
