####简单 Kafka 程序演示生产者发送 消费者消费消息  
####生产者发送消息的三种模式  
1. 发送并忘记（不接收返回值）  
`producer.send(record);//发送并忘记（不等待发送成功的返回 如果发送失败 内部也会重试）
`
2. 同步发送消息（接收Future类型的返回值 未来某个时候get阻塞获取发送结果)
    ```
    ...
    Future<RecordMetadata> future = producer.send(record);
    System.out.println("do other sth");
    RecordMetadata recordMetadata = future.get();//阻塞在这个位置
    ...
    ```  
3. 异步发送 (callback回调获取发送结果)  
    ```aidl
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
    ```  
####生产者KafkaProducer对象是线程安全的  
KafkaProducer 是线程安全的 可以多个线程使用同一个KafkaProducer对象 发送不同消息 不会相互影响。  

