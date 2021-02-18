package cn.pency.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author King老师   享学课堂 https://enjoy.ke.qq.com
 * 往期课程咨询芊芊老师  QQ：2130753077 VIP课程咨询 依娜老师  QQ：2133576719
 * 类说明：
 */
public class KafkaConst {

    /*生产者和消费者共用配置常量*/
    public static final String LOCAL_BROKER = "127.0.0.1:9092";
    public static final String BROKER_LIST = "127.0.0.1:9093,127.0.0.1:9094,127.0.0.1:9095";

    /*======================生产者配置============================*/
    public static Properties producerConfig(
            Class<? extends Serializer> keySerializeClazz,
            Class<? extends Serializer> valueSerializeClazz){
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,LOCAL_BROKER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,keySerializeClazz);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,valueSerializeClazz);
        return properties;
    }

    /*======================消费者配置========================*/
    public static Properties consumerConfig(String groupId,
            Class<? extends Deserializer> keyDeserializeClazz,
            Class<? extends Deserializer> valueDeserializeClazz){
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,LOCAL_BROKER);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,keyDeserializeClazz);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializeClazz);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        return properties;
    }

    public static Map<String,Object> consumerConfigMap(String groupId,
             Class<? extends Deserializer> keyDeserializeClazz,
             Class<? extends Deserializer> valueDeserializeClazz){
        Map<String,Object> properties = new HashMap<String, Object>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,LOCAL_BROKER);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,keyDeserializeClazz);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,valueDeserializeClazz);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        return properties;
    }



}
