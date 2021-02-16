package cn.pency.selfserial;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import cn.pency.vo.DemoUser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;

/**
 * @author pency
 * 类说明：
 */
public class SelfSerialConsumer {

    private static KafkaConsumer<String,DemoUser> consumer = null;

    public static void main(String[] args) {

        /*消息消费者*/
        consumer = new KafkaConsumer<String, DemoUser>(
                KafkaConst.consumerConfig("selfserial",
                StringDeserializer.class,
                SelfDeserializer.class));
        try {
            consumer.subscribe(Collections.singletonList(BusiConst.SELF_SERIAL_TOPIC));
            while(true){
                ConsumerRecords<String, DemoUser> records
                        = consumer.poll(Duration.ofMillis(500));
                for(ConsumerRecord<String, DemoUser> record:records){
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
