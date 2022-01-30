package ir.rahyab.hermes.adm.subscribersadmin.subscribersadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
/**
 * The {@code SubscribersAdminApplication} class is the starting point of the Subscribers Admin system.
 *
 * @author tahbaz
 */
@SpringBootApplication
public class SubscribersAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscribersAdminApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(){

		return new RestTemplate();
	}
}
