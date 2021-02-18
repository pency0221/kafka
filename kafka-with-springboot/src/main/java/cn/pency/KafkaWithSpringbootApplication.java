package cn.pency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaWithSpringbootApplication {

    public static void main(String[] args) {
        //默认情况下，启动Tomcat(8080)
        SpringApplication.run(KafkaWithSpringbootApplication.class, args);
    }
}
