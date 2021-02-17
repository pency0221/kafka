package cn.pency.rebalance;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author pency
 * 类说明：每个分区的偏移量记录在数据表中  提交偏移量和消费业务数据在一个事务中操作
 * 并添加分区再均衡监视器，一旦发生分区再均衡 也通过seek方法指定消费者从新分区最后提交的偏移量开始消费
 */
public class ConsumerWorker  implements Runnable{

    private final KafkaConsumer<String,String> consumer;
    //TODO    用来保存每个消费者当前读取分区的偏移量,解决分区再均衡的关键
    private final Map<TopicPartition, OffsetAndMetadata> currOffsets;
    private final boolean isStop;
    //TODO 事务类可以送入（tr）
    //private final Transaction  tr  事务类的实例

    public ConsumerWorker(boolean isStop) {
        /*消息消费者配置*/
        Properties properties = KafkaConst.consumerConfig(RebalanceConsumer.GROUP_ID,StringDeserializer.class,StringDeserializer.class);
        //TODO 取消自动提交
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,false);
        this.isStop = isStop;
        this.consumer = new KafkaConsumer<String, String>(properties);
        //TODO 偏移量
        this.currOffsets = new HashMap<TopicPartition, OffsetAndMetadata>();
        //TODO 消费者订阅时加入再均衡监听器(HandlerRebalance)
        consumer.subscribe(Collections.singletonList(BusiConst.REBALANCE_TOPIC), new HandlerRebalance(currOffsets,consumer));
    }

    public void run() {
        final String id = Thread.currentThread().getId()+"";
        int count = 0;
        TopicPartition topicPartition = null;
        long offset = 0;
        try {
            while(true){
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                //todo 业务处理
                //开始事务 tr.begin
                for(ConsumerRecord<String, String> record:records){
                    System.out.println(id+"|"+String.format( "处理主题：%s，分区：%d，偏移量：%d，" +"key：%s，value：%s",record.topic(),record.partition(),record.offset(),record.key(),record.value()));
                    topicPartition = new TopicPartition(record.topic(),record.partition());
                    offset = record.offset()+1;
                    //TODO 消费者消费时把偏移量提交到统一HashMap
                    currOffsets.put(topicPartition,new OffsetAndMetadata(offset,"no"));
                    count++;
                    //执行业务sql
                }
                //TODO 同时将业务和偏移量入库
                if(currOffsets.size()>0){
                    for(TopicPartition topicPartitionkey:currOffsets.keySet()){
                        HandlerRebalance.partitionOffsetMap.put(topicPartitionkey,currOffsets.get(topicPartitionkey).offset());
                    }
                }
                //TODO 提交事务
                //提交业务数和偏移量入库  tr.commit

                //TODO 如果stop参数为true,这个消费者消费到第5个时自动关闭
                if(isStop&&count>=5){
                    System.out.println(id+"-将关闭，当前偏移量为："+currOffsets);
                    consumer.commitSync();
                    break;
                }
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }
}
