package cn.pency;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Arrays;
import java.util.Properties;

/**
 * @author pency
 */
public class StreamDeal {
    public static void main(String[] args) throws  Exception {
        //TODO 流处理三个属性必须指定(broker地址清单、key和value的序列化器)
        Properties properties = new Properties();
        properties.put("bootstrap.servers","127.0.0.1:9092");
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG,"phone_count");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        //设计模式(建造者模式)
        StreamsBuilder builder = new StreamsBuilder();

        //使用流进行统计，(oppo 15  huawei 80  )  类似于数据表(结果)
        KStream<String,String> countStream  = builder.stream("phone");

        //函数编程
        KTable<String,Long> wordscount = countStream
                .flatMapValues(textLine -> Arrays.asList(textLine.toLowerCase().split(" ")))
                .groupBy((key,word) ->word) //分组
                .count(Materialized.<String,Long, KeyValueStore<Bytes, byte[]>>as("counts"));//计数
        wordscount.toStream().to("phone_cout", Produced.with(Serdes.String(),Serdes.Long())); //相当于 流处理的结果写入到另外一个主题
        //这里才是定义一个KafkaStreams的对象

        KafkaStreams streams = new KafkaStreams(builder.build(),properties);
        //启动Stream引擎
        streams.start();
        Thread.sleep(5000L); //休眠的时间
        builder.build();
        //将Topic中的流中的数据小写，同时，转换成数组，最后变成List

    }
}
