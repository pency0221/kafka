package cn.pency.commit;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author pency
 * 类说明：特定提交
 */
public class CommitSpecial {
    public static void main(String[] args) {
        /*消息消费者*/
        Properties properties = KafkaConst.consumerConfig(
                "CommitSpecial",
                StringDeserializer.class,
                StringDeserializer.class);
        //TODO 必须做
        /*取消自动提交*/
        properties.put("enable.auto.commit",false);

        KafkaConsumer<String,String> consumer= new KafkaConsumer<String, String>(properties);
        //todo 要提交的主题分区 和 偏移量
        Map<TopicPartition, OffsetAndMetadata> currOffsets= new HashMap<TopicPartition, OffsetAndMetadata>();
        int count = 0;
        try {
            consumer.subscribe(Collections.singletonList( BusiConst.CONSUMER_COMMIT_TOPIC));
            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for(ConsumerRecord<String, String> record:records){
                    System.out.println(String.format( "主题：%s，分区：%d，偏移量：%d，key：%s，value：%s",record.topic(),record.partition(),record.offset(), record.key(),record.value()));
                    currOffsets.put(new TopicPartition(record.topic(),record.partition()),new OffsetAndMetadata(record.offset()+1,"no meta"));
                    //todo 每隔11个提交一次
                    if(count%11==0){
                        //TODO 这里特定提交（异步方式，加入偏移量），每11条提交一次
                        consumer.commitAsync(currOffsets,null);
                    }
                    count++;
                }
            }
        } finally {
            //TODO 在关闭前最好同步提交一次偏移量
            consumer.commitSync();
            consumer.close();
        }
    }
}
