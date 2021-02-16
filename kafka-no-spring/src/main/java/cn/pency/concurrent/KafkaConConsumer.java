package cn.pency.concurrent;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pency
 * 类说明：多线程下正确的使用消费者，需要记住，一个线程一个消费者
 */
public class KafkaConConsumer {

    private static ExecutorService executorService = Executors.newFixedThreadPool(BusiConst.CONCURRENT_PARTITIONS_COUNT);

    private static class ConsumerWorker implements Runnable{

        private KafkaConsumer<String,String> consumer;
        //TODO 使用KafkaConsumer的实例要小心，应该每个消费数据的线程拥有自己的KafkaConsumer实例
        public ConsumerWorker(Map<String, Object> config, String topic) {
            Properties properties = new Properties();
            properties.putAll(config);
            this.consumer = new KafkaConsumer<String, String>(properties);
            //消费者线程不安全
            consumer.subscribe(Collections.singletonList(topic));
        }

        public void run() {
            final String id = Thread.currentThread().getId()
                    +"-"+System.identityHashCode(consumer);
            try {
                while(true){
                    ConsumerRecords<String, String> records= consumer.poll(Duration.ofMillis(500));
                    for(ConsumerRecord<String, String> record:records){
                        System.out.println(id+"|"+String.format(
                                "主题：%s，分区：%d，偏移量：%d，" +
                                        "key：%s，value：%s",
                                record.topic(),record.partition(),
                                record.offset(),record.key(),record.value()));
                        //do our work
                    }
                }
            } finally {
                consumer.close();
            }
        }
    }

    public static void main(String[] args) {
        /*消费配置的实例*/
        Map<String, Object> config
                = KafkaConst.consumerConfigMap("concurrent",
                StringDeserializer.class,
                StringDeserializer.class);
        for(int i = 0; i<BusiConst.CONCURRENT_PARTITIONS_COUNT; i++){
            executorService.submit(new ConsumerWorker(config,
                    BusiConst.CONCURRENT_USER_INFO_TOPIC));
        }
    }




}
