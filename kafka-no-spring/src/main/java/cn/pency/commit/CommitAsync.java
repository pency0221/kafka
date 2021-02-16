package cn.pency.commit;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 *
 * 类说明：异步手动提交当偏移量
 */
public class CommitAsync {

    public static void main(String[] args) {
        /*消息消费者*/
        Properties properties = KafkaConst.consumerConfig(
                "CommitAsync",
                StringDeserializer.class,
                StringDeserializer.class);
        //TODO  取消自动提交
        /*取消自动提交*/
        properties.put("enable.auto.commit",false);

        KafkaConsumer<String,String> consumer
                = new KafkaConsumer<String, String>(properties);
        try {
            consumer.subscribe(Collections.singletonList(
                    BusiConst.CONSUMER_COMMIT_TOPIC));
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
                //TODO 异步提交偏移量
                consumer.commitAsync();
                /*允许执行回调*/
                consumer.commitAsync(new OffsetCommitCallback() {
                    public void onComplete(
                            Map<TopicPartition, OffsetAndMetadata> offsets,
                            Exception exception) {
                        if(exception!=null){
                            System.out.print("Commmit failed for offsets ");
                            System.out.println(offsets);
                            exception.printStackTrace();
                        }
                    }
                });

            }
        } finally {
            consumer.close();
        }
    }
}
