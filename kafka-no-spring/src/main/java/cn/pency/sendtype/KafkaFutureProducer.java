package cn.pency.sendtype;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.concurrent.Future;

/**
* @author pency
 * 类说明：同步发送消息--未来某个时候get发送结果
 */
public class KafkaFutureProducer {

    private static KafkaProducer<String,String> producer = null;

    public static void main(String[] args) {

        /*消息生产者*/
        producer = new KafkaProducer<String, String>(KafkaConst.producerConfig(StringSerializer.class, StringSerializer.class));
        try {
            /*待发送的消息实例*/
            ProducerRecord<String,String> record;
            try {
                record =  new ProducerRecord<String,String>(
                        BusiConst.HELLO_TOPIC,"teacher10","james");
                Future<RecordMetadata> future = producer.send(record);
                System.out.println("do other sth");
                RecordMetadata recordMetadata = future.get();//阻塞在这个位置
                if(null!=recordMetadata){
                    System.out.println("offset:"+recordMetadata.offset()+"-" +"partition:"+recordMetadata.partition());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } finally {
            producer.close();
        }
    }




}
