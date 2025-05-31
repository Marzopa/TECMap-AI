package API;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"API", "Classroom", "Ollama", "Utils", "Repo"})
@EnableJpaRepositories(basePackages = "Repo")
@EntityScan(basePackages = "Classroom")
public class MicroserviceApp {
    public static void main(String[] args) {
        System.out.println("MicroserviceApp starting...");
        SpringApplication.run(MicroserviceApp.class, args);
    }
}
