package cn.pency.commit;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.CommitFailedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 * @author pency
 * 类说明：同步和异步组合
 */
public class SyncAndAsync {
    public static void main(String[] args) {
        /*消息消费者*/
        Properties properties = KafkaConst.consumerConfig("SyncAndAsync",
                StringDeserializer.class,
                StringDeserializer.class);
        //TODO 取消自动提交
        /*取消自动提交*/
        properties.put("enable.auto.commit",false);
        KafkaConsumer<String,String> consumer= new KafkaConsumer<String, String>(properties);
        try {
            consumer.subscribe(Collections.singletonList(BusiConst.CONSUMER_COMMIT_TOPIC));
            while(true){
                ConsumerRecords<String, String> records= consumer.poll(Duration.ofMillis(500));
                for(ConsumerRecord<String, String> record:records){
                    System.out.println(String.format(
                            "主题：%s，分区：%d，偏移量：%d，key：%s，value：%s",
                            record.topic(),record.partition(),record.offset(),
                            record.key(),record.value()));
                    //do our work
                }
                //TODO 异步提交
                consumer.commitAsync();
            }
        } catch (CommitFailedException e) {
            System.out.println("Commit failed:");
            e.printStackTrace();
        } finally {
            try {
                //TODO 为了万不一失，最后同步提交下
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
