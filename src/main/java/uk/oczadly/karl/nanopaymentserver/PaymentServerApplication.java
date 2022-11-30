package uk.oczadly.karl.nanopaymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
//@EnableTransactionManagement todo
public class PaymentServerApplication {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(PaymentServerApplication.class, args);
	}
	
}
