package tire.workshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import tire.workshop.config.WorkshopProperties;

@SpringBootApplication
@EnableConfigurationProperties(WorkshopProperties.class)
public class WorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkshopApplication.class, args);
    }
}
