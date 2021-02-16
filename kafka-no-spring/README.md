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
####生产者更多的属性  
属性过多，大部分都不需要更改 使用默认即可。除三个必须执行的属性（brokerip、key和value的序列化器）外需要留意下下面几个即可：  
   ```  
   //TODO 更多发送配置（重要的）
     properties.put("acks","1"); //ack 0,1,all  指的是broker确认接收到消息的程度 0直接ack 1首领分区成功写入就ack  all包括所有副本都复制写入后才ack
     properties.put("batch.size",16384); // 一个批次可以使用的内存大小 缺省16384(16k) 发往同一个分区的消息缓存到了16k就发送一批
     properties.put("linger.ms",0L); // 指定了生产者在发送批次前等待更多消息加入批次的时间, 缺省0  即不等待 来一条就发送
     properties.put("max.request.size",1 * 1024 * 1024); // 控制生产者发送请求（1个或一批次消息的总限制）最大大小,默认1M （这个参数和Kafka主机的message.max.bytes参数有关系 要小于Kafka主机的限制）
   ```    
####如何严格保证消息有序？
1.首先保证这个主题就一个分区，因为kafka保证同一分区内消息有序。同时并发横向消费多个分区 无法保证有序。  
2.要保证生产者发送消息 和broker成功接收到的消息是顺序是一致的。 正常情况下都是一致的 但是有消息发送失败 api其他线程内重试 就可能发送生产者要发送的后续消息先发送成功 导致生产者发送消息和broker接收顺序不一致导致乱序
所以如果需要严格保证消息的顺序性（主题就一个分区是保证消息有序的前提）那么就需要把max.in.flight.request.per.connection 设为 1（进制前一个消息没有响应接收成功之前 不可再发送其他消息），但是会严重影响生产者的吞吐量，所以只有在对消息的顺序有严格要求的情况下才能这么做。  
