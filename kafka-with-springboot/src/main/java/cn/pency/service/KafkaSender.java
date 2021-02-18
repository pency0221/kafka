package cn.pency.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author pency
 * 类说明：
 */
@Component
public class KafkaSender {

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void messageSender(String tpoic,String key,String message){
        try {
            kafkaTemplate.send(tpoic,key,message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
