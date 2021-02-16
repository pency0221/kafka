package cn.pency.ConsumerConfig;

import cn.pency.config.BusiConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

/**
 *
 * 类说明：
 */
public class ConfigKafkaConsumer {

    public static void main(String[] args) {
        //TODO 消费者三个属性必须指定(broker地址清单、key和value的反序列化器)
        Properties properties = new Properties();
        properties.put("bootstrap.servers","127.0.0.1:9092");
        properties.put("key.deserializer", StringDeserializer.class);
        properties.put("value.deserializer", StringDeserializer.class);
        //TODO 群组并非完全必须
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"test1");

        //TODO 更多消费者配置（重要的）
        properties.put("auto.offset.reset","latest"); //消费者在读取一个没有偏移量的分区或者偏移量无效的情况下，如何处理
        properties.put("enable.auto.commit",true); // 表明消费者是否自动提交偏移 默认值true
        properties.put("max.poll.records",500); // 控制每次poll方法返回的的记录数量 默认值500
        //TODO 分区分配给消费者的策略。系统提供两种策略。默认为Range 。 可以自定义策略后使用"partition.assignment.strategy"指定即可
        properties.put("partition.assignment.strategy",Collections.singletonList(RangeAssignor.class));


        KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);
        try {
            //TODO 消费者订阅主题（可以多个）
            consumer.subscribe(Collections.singletonList(BusiConst.HELLO_TOPIC));
            while(true){
                //TODO 拉取（新版本）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for(ConsumerRecord<String, String> record:records){
                    System.out.println(String.format("topic:%s,分区：%d,偏移量：%d," + "key:%s,value:%s",record.topic(),record.partition(),
                            record.offset(),record.key(),record.value()));
                    //do my work
                    //打包任务投入线程池
                    // ex
                }
            }
        } finally {
            consumer.close();
        }

    }




}
