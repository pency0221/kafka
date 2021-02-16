package cn.pency.selfpartition;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

/**
 * @author pency
 * 类说明：自定义分区器，以value值进行分区
 */
public class SelfPartitioner implements Partitioner {
    public int partition(String topic, Object key, byte[] keyBytes,
                         Object value, byte[] valueBytes, Cluster cluster) {
        //TODO 拿到主题的分区信息
        List<PartitionInfo> partitionInfos = cluster.partitionsForTopic(topic);
        //TODO 分区数
        int num = partitionInfos.size();
        //TODO 例如 根据value哈希值与分区数求余的方式得到分区ID
        int parId = ((String)value).hashCode()%num;
        return parId;
    }

    public void close() {
        //do nothing
    }

    public void configure(Map<String, ?> configs) {
        //do nothing
    }

}
