package cn.pency.rebalance;

import cn.pency.config.BusiConst;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pency
 * 类说明：设置了再均衡监听器的消费者
 */
public class RebalanceConsumer {

    public static final String GROUP_ID = "rebalanceconsumer";

    //TODO 使用线程池，两个线程（两个消费者）
    private static ExecutorService executorService = Executors.newFixedThreadPool(BusiConst.CONCURRENT_PARTITIONS_COUNT);


    public static void main(String[] args) throws InterruptedException {
        //TODO 步骤1，起两个消费者，消费三个分区
        for(int i = 0; i<BusiConst.CONCURRENT_PARTITIONS_COUNT; i++){
            executorService.submit(new ConsumerWorker(false));
        }
        Thread.sleep(5000);
        //用来被停止，观察保持运行的消费者情况
        //TODO 步骤2，再起一个消费者，这个消费者消费了5条数据后再关闭。
        //TODO (这里会触发两次分区再均衡，一次是新消费者加入，另一次是这个消费者消费一段后离开)
        new Thread(new ConsumerWorker(true)).start();
    }
}
