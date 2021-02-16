package cn.pency.sendtype;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

/**
* @author pency
 * 类说明：发送消息--异步模式
 */
public class KafkaAsynProducer {

    private static KafkaProducer<String,String> producer = null;

    public static void main(String[] args) {
        /*消息生产者*/
        producer = new KafkaProducer<String, String>(
                KafkaConst.producerConfig(StringSerializer.class,
                StringSerializer.class));
        /*待发送的消息实例*/
        ProducerRecord<String,String> record;
        try {
            record = new ProducerRecord<String,String>(
                    BusiConst.HELLO_TOPIC,"teacher14","deer");
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata metadata,Exception exception) {
                    if(null!=exception){
                        //发送失败的打印出来错误信息
                        exception.printStackTrace();
                    }
                    if(null!=metadata){
                        //todo 成功的 可以获取分区和偏移量等元数据信息
                        System.out.println("offset:"+metadata.offset()+"-"+"partition:"+metadata.partition());
                    }
                }
            });
        } finally {
            producer.close();
        }
    }




}
