package cn.pency.hellokafka;

import cn.pency.config.BusiConst;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author pency
 * 类说明：kafka生产者
 */
public class HelloKafkaProducer {
    public static void main(String[] args) {
        //TODO 创建生产者时三个属性必须指定(broker地址清单、key和value的序列化器)
        Properties properties = new Properties();
        properties.put("bootstrap.servers","127.0.0.1:9092");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //todo 序列化
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String,String> producer = new KafkaProducer<String, String>(properties);
        try {
            ProducerRecord<String,String> record;
            try {
                //TODO 发送4条消息
                for(int i=0;i<4;i++){
                    //todo 把消息包装成ProducerRecord对象进行发送 指定主题、key（可选）、消息value
                    record = new ProducerRecord<String,String>(BusiConst.HELLO_TOPIC, null,"lison");
                    //todo 发送
                    producer.send(record);//发送并发忘记（不等待发送成功的返回 如果发送失败 内部也会重试）
                    System.out.println(i+"，message is sent");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            producer.close();
        }
    }
}
