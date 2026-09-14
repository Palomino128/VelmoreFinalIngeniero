package pe.edu.velmore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // Habilita las tareas programadas (Cron Jobs)
public class VelmoreLibreriasApplication {
    public static void main(String[] args) {
        SpringApplication.run(VelmoreLibreriasApplication.class, args);
    }
}

