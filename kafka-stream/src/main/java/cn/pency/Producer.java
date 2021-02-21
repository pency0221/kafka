package cn.pency;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Random;

/**
 * 交易系统（生产者类)
 */
public class Producer {
    //kafka生产者对象
    private static KafkaProducer<String,String> producer = null;

    public static void main(String[] args) {
        /*发送配置的实例   数据库 JDBC 连接*/
        Properties properties = new Properties();
        /*broker的地址清单*/
        properties.put("bootstrap.servers","127.0.0.1:9092");
        /*key的序列化器*/
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /*value的序列化器*/
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /*消息生产者*/
        producer = new KafkaProducer<String, String>(properties);

        //商城（各色商品）
        String[] goods = {"iphone","huawei","mi","oppo","vivo"};
        Random r = new Random();//随机数
        Random r1 = new Random();//随机数
        try {//业务
            long startTime = System.currentTimeMillis();//开始时间
            /*待发送的消息实例*/
            ProducerRecord<String,String> record;
            //生成高并发场景下的请求(循环一万次)
            for(int i=1;i<10000;i++){
                int goodscount = r.nextInt(10);//随机生成一次购买商品的数量
                StringBuilder sb = new StringBuilder("");//商品列表
                if (goodscount ==0) continue; //避免生成value是空的
                for(int j=0;j<goodscount;j++){
                    //根据商品的数量，生成随机的商品信息，每件商品使用 空格分隔，例如：3个iphone huawei mi
                    sb.append(goods[r1.nextInt(goods.length)]).append(" ");
                }
                try {
                    record = new ProducerRecord<String,String>("phone","sell",sb.toString());
                    producer.send(record);        /*发送消息--发送后不管*/
                    System.out.println("用户请求的商品："+sb.toString());
                    Thread.sleep(2); //1秒钟发送500条(不考虑往Kafka中送入的耗时) 20多秒可以发送完
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            long end = System.currentTimeMillis();//结束时间
            float seconds = (end - startTime) / 1000F;
            System.out.println("生产者数据消耗时间:"+Float.toString(seconds) + " seconds.");
        } finally {
            producer.close();
        }
    }




}
