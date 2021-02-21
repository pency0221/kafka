package cn.pency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaTrafficShapingClient {

    public static void main(String[] args) {
        SpringApplication.run(KafkaTrafficShapingClient.class, args);
    }
}
