package cn.pency.consumergroup;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author pency
 * 类说明：
 */
public class GroupAConsumer2 {

    private static KafkaConsumer<String,String> consumer = null;

    public static void main(String[] args) {
        /*消费配置的实例*/
        Properties properties
                = KafkaConst.consumerConfig(BusiConst.CONSUMER_GROUP_A,
                StringDeserializer.class,
                StringDeserializer.class);
        /*消息消费者*/
        consumer = new KafkaConsumer<String, String>(properties);

        try {
            consumer.subscribe(Collections.singletonList(BusiConst.CONSUMER_GROUP_TOPIC));
            consumer.poll(0);
            while(true){
                ConsumerRecords<String, String> records
                        = consumer.poll(Duration.ofMillis(500));
                for(ConsumerRecord<String, String> record:records){
                    System.out.println(String.format(
                            "主题：%s，分区：%d，偏移量：%d，key：%s，value：%s",
                            record.topic(),record.partition(),record.offset(),
                            record.key(),record.value()));
                    //do our work
                }
            }
        } finally {
            consumer.close();
        }
    }




}
