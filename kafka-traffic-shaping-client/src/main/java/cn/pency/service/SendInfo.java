package cn.pency.service;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

/**
 * @author pency
 * 类说明：
 */
@Component
public class SendInfo implements ProducerListener {
    @Override
    public void onSuccess(String topic, Integer partition,
                          Object key, Object value,
                          RecordMetadata recordMetadata) {
        System.out.println(String.format(
                "主题：%s，分区：%d，偏移量：%d，" +
                        "key：%s，value：%s",
                recordMetadata.topic(),recordMetadata.partition(),
                recordMetadata.offset(),key,value));

    }

    @Override
    public void onError(String topic, Integer partition,
                        Object key, Object value, Exception exception) {
        exception.printStackTrace();
    }

    @Override
    public boolean isInterestedInSuccess() {
        return true;
    }
}
