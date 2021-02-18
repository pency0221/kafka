package cn.pency.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.MessageListener;

/**
 * @author pency
 * 类说明：
 */
public class KafkaConsumer  implements MessageListener<String,String> {

    public void onMessage(ConsumerRecord<String, String> data) {
        String name = Thread.currentThread().getName();
        System.out.println(name+"|"+String.format(
                "主题：%s，分区：%d，偏移量：%d，key：%s，value：%s",
                data.topic(),data.partition(),data.offset(),
                data.key(),data.value()));
    }
}

