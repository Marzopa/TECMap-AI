package Frontend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"Frontend", "Classroom", "Ollama", "Utils"})
public class MicroserviceApp {
    public static void main(String[] args) {
        System.out.println("MicroserviceApp starting...");
        SpringApplication.run(MicroserviceApp.class, args);
    }
}
