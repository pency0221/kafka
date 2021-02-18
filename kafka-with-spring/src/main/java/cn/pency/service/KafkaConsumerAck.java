package cn.pency.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;


/**
 * @author pency
 * 类说明：
 */
public class KafkaConsumerAck implements AcknowledgingMessageListener<String,String> {

    public void onMessage(ConsumerRecord<String, String> data,
                          Acknowledgment acknowledgment) {
        String name = Thread.currentThread().getName();
        System.out.println(name+"|"+String.format(
                "主题：%s，分区：%d，偏移量：%d，key：%s，value：%s",
                data.topic(),data.partition(),data.offset(),
                data.key(),data.value()));
        //偏移量确认（手动的过程）
        acknowledgment.acknowledge();

    }
}
