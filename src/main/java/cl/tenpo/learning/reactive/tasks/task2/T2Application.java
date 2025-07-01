package cl.tenpo.learning.reactive.tasks.task2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@EnableR2dbcRepositories
@EnableReactiveMongoRepositories
@ComponentScan(basePackages = "cl.tenpo.learning.reactive")
@SpringBootApplication
public class T2Application {

    public static void main(String[] args) {
        SpringApplication.run(T2Application.class, args);
    }

}
