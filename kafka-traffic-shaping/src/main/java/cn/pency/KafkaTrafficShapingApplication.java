package cn.pency;

import cn.pency.service.SpringContextUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class KafkaTrafficShapingApplication {

    public static void main(String[] args) {
        ApplicationContext app =
                SpringApplication.run(KafkaTrafficShapingApplication.class,
                        args);
        SpringContextUtil.setApplicationContext(app);
    }
}
