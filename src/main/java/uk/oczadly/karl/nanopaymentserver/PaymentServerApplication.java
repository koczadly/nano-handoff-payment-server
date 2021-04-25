package uk.oczadly.karl.nanopaymentserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.oczadly.karl.jnano.model.block.StateBlock;
import uk.oczadly.karl.nanopaymentserver.properties.HandoffProperties;
import uk.oczadly.karl.nanopaymentserver.properties.NodeProperties;
import uk.oczadly.karl.nanopaymentserver.service.RpcService;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class PaymentServerApplication {
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(PaymentServerApplication.class, args);
	}
	
}
