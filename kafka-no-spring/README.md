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
####序列化
创建生产者对象必须指定序列化器，默认的序列化器并不能满足我们所有的场景。我们完全可以自定义序列化器。只要实现org.apache.kafka.common.serialization.Serializer 接口即可。（同时要和消费端协商好规则，使用配套的反序列化器能再正常反序列化回来即可）。

**自定义序列化需要考虑的问题：**
> 自定义序列化容易导致程序的脆弱性,每个消费者对消息实体字段都有各自的需求，比如，有的将字段变更为 long 型，有的会增加字段，这样会出现新旧消息的兼容性问题  
> 解决这个问题，可以考虑使用自带格式描述以及语言无关的序列化框架，比如Kafka 官方推荐的 Apache Avro。Avro 会使用一个 JSON 文件作为 schema 来描述数据，Avro 在读写时会用到这个 schema，可以把这个 schema 内嵌在数据文件中。这样，不管数据格式如何变动，消费者都知道如何处理数据。但是内嵌的消息，自带格式，会导致消息的大小不必要的增大，消耗了资源。  
> 我们可以使用 schema 注册表机制，将所有写入的数据用到的 schema保存在注册表中，然后在消息中引用 schema 的标识符，而读取的数据的消费者程序使用这个标识符从注册表中拉取 schema 来反序列化记录。  
> 注意：Kafka 本身并不提供 schema 注册表，需要借助第三方，现在已经有很多的开源实现，比如 Confluent Schema Registry，可以从 GitHub 上获取。如何使用参考如下网址：https://cloud.tencent.com/developer/article/1336568
不过一般除非使用 Kafka 需要关联的团队比较大、敏捷开发团队才会使用，一般的团队 对于一般的情况使用 JSON 足够了。  

####分区
创建ProducerRecord 对象时需指定主题、键和值，Kafka 的消息都是一个个的键值对。键可以设置为默认的 null。  
键的主要用途有两个：一，用来决定消息被写往主题的哪个分区，拥有相同键的消息将被写往同一个分区，二，还可以作为消息的附加消息。  

**kafka默认的分区器针对key决定分区：**  
1. 如果键值为 null，分区器使用轮询算法将消息均衡地分布到各个分区上。    
2. 如果键不为空，Kafka 对键进行散列（Kafka 自定义的散列算法），然后根据散列值把消息映射到特定的分区上。很明显，同一个键（非null）总是被映射到同一个分区。   
ps:只有不改变主题分区数量的情况下，键和分区之间的映射才能保持不变，一旦增加了新的分区，就无法保证了，所以如果要使用键来映射分区，那就要在创建主题的时候把分区规划好，而且永远不要增加新分区。    
