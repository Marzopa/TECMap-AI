package Frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Tells Spring Boot to scan for components in this package and subpackages
public class MicroserviceApp {
    public static void main(String[] args) {
        SpringApplication.run(MicroserviceApp.class, args);
    }
}
