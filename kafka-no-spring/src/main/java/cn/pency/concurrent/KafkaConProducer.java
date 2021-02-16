package cn.pency.concurrent;

import cn.pency.config.BusiConst;
import cn.pency.config.KafkaConst;
import cn.pency.vo.DemoUser;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author pency
 * 类说明：多线程下使用生产者
 */
public class KafkaConProducer {

    //发送消息的个数
    private static final int MSG_SIZE = 1000;
    //负责发送消息的线程池
    private static ExecutorService executorService= Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static CountDownLatch countDownLatch= new CountDownLatch(MSG_SIZE);

    private static DemoUser makeUser(int id){
        DemoUser demoUser = new DemoUser(id);
        String userName = "xiangxue_"+id;
        demoUser.setName(userName);
        return demoUser;
    }

    /*发送消息的任务*/
    private static class ProduceWorker implements Runnable{

        private ProducerRecord<String,String> record;
        private KafkaProducer<String,String> producer;

        public ProduceWorker(ProducerRecord<String, String> record, KafkaProducer<String, String> producer) {
            this.record = record;
            this.producer = producer;
        }

        public void run() {
            final String id = Thread.currentThread().getId()+"-"+System.identityHashCode(producer);
            try {
                producer.send(record, new Callback() {
                    public void onCompletion(RecordMetadata metadata,Exception exception) {
                        if(null!=exception){
                            exception.printStackTrace();
                        }
                        if(null!=metadata){
                            System.out.println(id+"|"+String.format("偏移量：%s,分区：%s",metadata.offset(),metadata.partition()));
                        }
                    }
                });
                System.out.println(id+":数据["+record+"]已发送。");
                countDownLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        KafkaProducer<String,String> producer= new KafkaProducer<String, String>(
                KafkaConst.producerConfig(StringSerializer.class,StringSerializer.class));
        try {
            //循环发送，通过线程池的方式
            for(int i=0;i<MSG_SIZE;i++){
                DemoUser demoUser = makeUser(i);
                ProducerRecord<String,String> record= new ProducerRecord<String,String>(
                                                            BusiConst.CONCURRENT_USER_INFO_TOPIC,
                                                            null,
                                                            System.currentTimeMillis(),
                                                            demoUser.getId()+"",
                                                            demoUser.toString());
                //todo KafkaProducer是线程安全的 可以多个线程使用同一个KafkaProducer对象 发送不同消息
                executorService.submit(new ProduceWorker(record,producer));
            }
            //todo 等待所有子线程线程启动并发送完消息
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
            executorService.shutdown();
        }
    }




}
