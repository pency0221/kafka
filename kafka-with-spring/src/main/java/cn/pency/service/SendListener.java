package cn.pency.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;

/**
 * @author pency
 * 类说明：发送者确认
 */
public class SendListener implements ProducerListener {

    public void onSuccess(String topic, Integer partition,
                          Object key, Object value, RecordMetadata recordMetadata) {
        System.out.println("offset:"+recordMetadata.offset()+"-"
                +"partition:"+recordMetadata.partition());
    }

    public void onError(String topic, Integer partition,
                        Object key, Object value, Exception exception) {

    }

    public boolean isInterestedInSuccess() {
        return true;
    }

}
