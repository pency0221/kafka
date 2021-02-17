####简单 Kafka 程序演示生产者发送 消费者消费消息  

##kakfa的生产者
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

#####kafka默认的分区器针对key决定分区效果
1. 如果键值为 null，分区器使用轮询算法将消息均衡地分布到各个分区上。    
2. 如果键不为空，Kafka 对键进行散列（Kafka 自定义的散列算法），然后根据散列值把消息映射到特定的分区上。很明显，同一个键（非null）总是被映射到同一个分区。   
ps:只有不改变主题分区数量的情况下，键和分区之间的映射才能保持不变，一旦增加了新的分区，就无法保证了，所以如果要使用键来映射分区，那就要在创建主题的时候把分区规划好，而且永远不要增加新分区。
#####自定义分区器
某些情况下，数据特性决定了需要进行特殊分区，比如电商业务，北京（一般是key）的业务量明显比较大，占据了总业务量的 20%，我们需要对北京的订单进行单独分区处理，默认的散列分区算法不合适了，也不均匀， 我们就可以自定义分区算法（implements Partitioner），对北京的订单单独处理，其他地区沿用散列分区算法。或者某些情况下，我们用 value 来进行分区。  
实现方法：
1. implements Partitioner写分区器
2. 创建生产者时通过属性“partitioner.class”指定使用自定义的分区器。  
   `   properties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "cn.pency.selfpartition.SelfPartitioner");
   `
##Kafka 的消费者
####群组  
Kafka 里消费者从属于消费者群组，一个群组里的消费者订阅的都是同一个主题，每个消费者接收主题一部分分区的消息。  
- 消费者群组消费固定数量的分区的消息，不断往群组添加消费者，会动态再平衡消费者和负责消费分区的对应关系。  
- 往消费者群组里增加消费者是进行横向伸缩能力的主要方式。所以我们有必要为主题设定合适规模的分区（分区数也就决定的并行度），在负载均衡的时候可以加入更多的消费者，但是一个分区只能被同一个群组里的一个消费者消费，意味着>分区数量的群组中多出来的消费者对提高效率是没有帮助的（备胎，前面消费者挂了，接盘它的分区）。  
- 如果是多个应用程序需要从同一个主题中读取数据，只要保证每个应用程序有自己的消费者群组就行了（和其他应用程序群组区分开，因为同一个分区只能被同一个群组里的一个消费者消费,多个群组就互不影响 被不同群组重复消费）。
   
指定群组：创建消费者时使用group.id属性指定。
   ```aidl
     //TODO 创建消费者时三个属性必须指定(broker地址清单、key和value的反序列化器)
     Properties properties = new Properties();
     properties.put("bootstrap.servers","127.0.0.1:9092");
     properties.put("key.deserializer", StringDeserializer.class);//todo 反序列化
     properties.put("value.deserializer", StringDeserializer.class);
     //TODO 群组并非完全必须
     properties.put("group.id","test1");
     KafkaConsumer<String,String> consumer = new KafkaConsumer<String, String>(properties);
   ```
####消费者配置  
同生产者，消费者也有很多属性可以设置，大部分都有合理的默认值，无需调整。有些参数可能对内存使用，性能和可靠性方面有较大影响。可以参考org.apache.kafka.clients.consumer 包下 ConsumerConfig 类。  
####订阅  
创建消费者后，使用 subscribe()方法订阅主题，这个方法接受一个主题列表为参数，也可以接受一个正则表达式为参数；  
如果新创建了新主题，并且主题名字和正则表达式匹配，那么会立即触发一次再均衡，消费者就可以读取新添加的主题。  
比如，要订阅所有和 test相关的主题，可以 subscribe(“tets.*”)  
####轮询poll消息  
为了不断的获取消息，我们要在循环中不断的进行轮询，也就是不停调用 poll 方法。  
poll 方法的参数为超时时间，控制 poll 方法的阻塞时间，它会让消费者在指定的毫秒数内一直等待 broker 返回数据。poll 方法将会返回一个记录（消息）列表，每一条记录都包含了记录所属的主题信息，记录所在分区信息，记录在分区里的偏移量，以及记录的键值对。  
####提交和偏移量
当我们调用 poll 方法的时候，broker 返回的是生产者写入 Kafka 但是还没有被这个群组的消费者读取过的记录，消费者可以使用 Kafka 来追踪消息在分区里的位置，我们称之为偏移量。   
消费者更新自己读取到了哪个消息的操作，我们称之为提交。  
消费者如何提交的偏移量？消费者会往一个叫做_consumer_offset 的特殊主题发送一个消息，里面会包括每个分区的偏移量。  
###消费者核心概念
####KafkaConsumer多线程不安全
KafkaConsumer 的实现不是线程安全的，所以我们在多线程的环境下，使用 KafkaConsumer的实例要小心（比如多个线程消费者订阅的主题发送混乱），应该每个消费数据的线程拥有自己的KafkaConsumer 实例。  
####群组协调
- 消费者要加入群组会向群组协调器发送一个 JoinGroup 请求，第一个加入群组的成为群主，群主能获得后续进群的成员列表并负责给每一个消费者分配分区。  
- 分配完毕后，群主把分配情况发送给群组协调器，协调器再把这些信息发送给所有的消费者，每个消费者只能看到自己的分配信息，只有群主知道群组里所有消费者的分配信息。  
- 群组协调的工作会在消费者发生变化(新加入或者掉线)，主题中分区发生了变化（增加）时发生。   
####分区再均衡  
- 当消费者群组里的消费者发生变化，或者主题里的分区发生了变化，都会导致再均衡现象的发生（重新分配分区和消费者所属关系）。Kafka 中存在着消费者对分区所有权的关系，这样无论是消费者变化需要重新分配分区关系，还是增加了分区，哪个消费者来读取这个新增的分区，这些行为，都会导致分区所有权的变化，这种变化就被称为再均衡。  
- 再均衡对 Kafka 很重要，这是消费者群组带来高可用性和伸缩性的关键所在。但尽量减少再均衡，因为再均衡期间，消费者无法读取消息会造成整个群组一小段时间的不可用。  
- 消费者通过向称为群组协调器的 broker（不同的群组有不同的协调器）发送心跳来维持它和群组的从属关系以及对分区的所有权关系。如果消费者长时间不发送心跳，群组协调器认为它已经死亡，就会触发一次再均衡。  
在 0.10.1 及以后的版本中，心跳由单独的线程负责，相关的控制参数为 max.poll.interval.ms。
####Kafka中的消费安全  
一般情况下，调用 poll 方法的时候，broker 返回的是生产者正常写入，消费者提交获取到消息的偏移量，这样可以确保消费者消息消费不丢失也不重复，所以一般情况下 Kafka 提供的原生的消费者是安全的。但是某些特殊情况下也可能出现消费安全问题（主要是消费者提交偏移量和客户端最后处理的最后一个消息偏移量不一致造成） 
**消费者提交偏移量可能导致的消费安全问题**  
消费者是如何提交偏移量的呢？消费者会往一个叫做_consumer_offset 的特殊主题发送一个消息，里面会包括每个分区的偏移量。  
如果消费者一直运行，偏移量的提交并不会产生任何影响。但是如果有消费者发生崩溃，或者有新的消费者加入消费者群组的时候，会触发 Kafka 的再均衡。这使得 Kafka 完成再均衡之后，每个消费者可能被会分到新分区中。为了能够继续之前的工作，消费者就需要读取每一个分区的最后一次提交的偏移量，然后从偏移量指定的地方继续处理。    
- 如果提交的偏移量小于消费者实际处理的最后一个消息的偏移量，处于两个偏移量之间的消息会被重复处理
- 如果提交的偏移量大于客户端处理的最后一个消息的偏移量,那么处于两个偏移量之间的消息将会丢失    
####消费者提交偏移量的多种方式  
1. 自动提交  
> 最简单的提交方式是让消费者自动提交偏移量。 创建消费者时 enable.auto.comnit 被设为 true（默认），消费者会自动把从 poll()方法接收到的最大偏移量提交上去。提交时间间隔由 auto.commit.interval.ms 控制，默认值是 5s。  
> 自动提交是在轮询里进行的，消费者每次在进行轮询时会检査是否该提交偏移量了，如果是，那么就会提交从上一次轮询返回的偏移量。  
> 不过,在使用这种简便的方式之前,需要知道它将会带来怎样的结果。假设我们仍然使用默认的 5s 提交时间间隔, 在最近一次提交之后的 3s 发生了再均衡，再均衡之后,消费者从最后一次提交的偏移量位置开始读取消息。这个时候偏移量已经落后了 3s，所以在这 3s 内到达的消息会被重复处理。可以通过修改提交时间间隔来更频繁地提交偏移量, 减小可能出现重复消息的时间窗, 不过这种情况是无法完全避免的 。  
> 在使用自动提交时,每次调用轮询方法都会把上一次调用返回的最大偏移量提交上去,它并不知道具体哪些消息已经被处理了,所以在再次调用之前最好确保所有当前调用返回的消息都已经处理完毕(enable.auto.comnit 被设为 true 时，在调用 close()方法之前也会进行自动提交)。一般情况下不会有什么问题,不过在处理异常或提前退出轮询时要格外小心。  
> 自动提交虽然方便,但是很明显是一种基于时间提交的方式,而且没有为我们留有余地来避免重复处理消息。
2. 手动提交  
消费者 API 提供了多种手动提交偏移量的方式，可以在必要的时候手动提交,而不是基于时间间隔自动提交。首先把 enable.auto.commit 设为 false关闭自动提交。  
- 同步  
  使用 commitsync()同步提交偏移量最简单也最可靠。这个方法会提交由 poll()方法返回的最新偏移量，提交成功后马上返回,如果发生不可恢复不可重试的提交失败就抛出异常。
注意： commitsync()将会提交由 poll()返回的最新偏移量,所以在处理完所有记录后要确保调用了 commitsync()，否则还是会有丢失进度的风险。如果发生了再均衡,从最近批消息到发生再均衡之间的所有消息都将被重复处理。  
只要没有发生不可恢复的错误，commitSync（）方法会阻塞，会一直尝试直至提交成功，如果失败，也只能记录异常日志。
具体使用，参见模块 kafka-no-spring 下包 commit 包中代码 CommitSync。
- 异步  
使用 commitsync()提交 在 broker 对提交请求作出回应之前，应用程序会一直阻塞。这时我们可以使用异步提交commitAsync（），只管发送提交请求，无需等待 broker的响应。commitAsync()也支持回调,在 broker 作出响应时会执行回调。回调经常被用于记录提交错误或生成度量指标。    
    ```aidl
       //TODO 异步提交偏移量
       consumer.commitAsync();
       /*允许执行回调*/
       consumer.commitAsync(new OffsetCommitCallback() {
           public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets,Exception exception) {
               if(exception!=null){
                   System.out.print("Commmit failed for offsets ");
                   System.out.println(offsets);
                   exception.printStackTrace();
               }
           }
       });
   ```  

在成功提交或碰到无法恢复的错误之前, commitsync()会一直重试,但是 commitAsync 不会。它之所以不进行重试,是因为在它收到服务器响应的时候,可能有一个更大的偏移量已经提交成功,如果它会重试提交之前提交失败的偏移量 则是倒退擦除了进度 如果这时再平衡 就会重复消费。  

- 同步和异步组合  
一般情况下,针对偶尔出现的提交失败,（异步提交）不进行重试不会有太大问题，因为如果提交失败是因为临时问题导致的,那么后续的提交总会有成功的。但如果这是发生在关闭消费者或再均衡前的最后一次提交,就要确保能够提交成功。  
因此,一般会组合使用 commitAsync()和 commitsync()，正常轮训消费是使用异步commitAsync()，保证万无一失最后一次关闭消费者时再finally里commitsync()同步提交一次。
- 特定提交  
在前面的手动提交中，提交偏移量的频率与处理消息批次的频率是一样的。但如果想要更频繁地提交该怎么办?  
如果poll()方法返回一大批数据,为了避免因再均衡引起的重复处理整批消息,想要在批次中间提交偏移量该怎么办?这种情况无法通过调用commitSync()或 commitAsync()来实现，因为它们只会提交最后一个偏移量,而此时该批次里的消息还没有处理完。消费者 API 允许在调用 commitsync()和 commitAsync()方法时传进去希望提交的分区和偏移量的 map。假设我们处理了半个批次的消息,最后一个来自主题“customers”，分区 3 的消息的偏移量是 5000，你可以调用 commitsync()方法来提交它。不过，因为消费者可能不只读取一个分区,因为我们需要跟踪所有分区的偏移量,所以在这个层面上控制偏移量的提交会让代码变复杂。  
使用方法：commitAsync()和 commitsync()都支持传入Map<TopicPartition, OffsetAndMetadata> offsets 对象指定分区和偏移量提交。    
   ```aidl
    //todo 构建包含要提交的主题分区 和 偏移量 信息的map
     Map<TopicPartition, OffsetAndMetadata> currOffsets= new HashMap<TopicPartition, OffsetAndMetadata>();
     int count = 0;
      consumer.subscribe(Collections.singletonList( BusiConst.CONSUMER_COMMIT_TOPIC));
      while(true){
          ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
          for(ConsumerRecord<String, String> record:records){
              //todo 分区及对应偏移量信息 放入map中
              currOffsets.put(new TopicPartition(record.topic(),record.partition()),new OffsetAndMetadata(record.offset()+1,"no meta"));
              //todo 每隔11个提交一次
              if(count%11==0){
                  //TODO 这里特定提交（加入偏移量map），每11条提交一次
                  consumer.commitAsync(currOffsets,null);
              }
              count++;
          }
      }
   ```
####分区再均衡监视器  
消费者在退出和进行分区再均衡之前,会做一些清理工作比如，提交偏移量、关闭文件句柄、数据库连接等。在为消费者分配新分区或移除旧分区时,通过监听再均衡可以在再均衡发生前后执行一些应用程序代码，方法是调用 subscribe()方法时传进去一个 ConsumerRebalancelistener再均衡监听器实例就可以了。  
ConsumerRebalancelistener 有两个需要实现的方法：
1) public void onPartitionsRevoked( Collection< TopicPartition> partitions)方法会在再均衡开始之前和消费者停止读取消息之后被调用。如果在这里提交偏移量，下一个接管分区的消费者就知道该从哪里开始读取了
2) public void onPartitionsAssigned( Collection< TopicPartition> partitions)方法会在重新分配分区之后和消费者开始读取消息之前被调用。   

PS：无论添加不添加再均衡监视器 再均衡都是自动发生  添加在均衡监视器只是为了监听到要发生再均衡了 在再均衡过程前后添加一些我们自己的操作
   
**从特定偏移量处开始记录  KafkaConsumer.seek方法**  
   如果想从分区的起始位置开始读取消息,或者直接跳到分区的末尾开始读取消息,可以使 seekToBeginning(Collection<TopicPartition> tp)和seekToEnd( Collection<TopicPartition>tp)这两个方法。  

即使我们毎处理一条记录就提交一次偏移量，在消息业务记录被保存到数据库之后以及偏移量被提交之前,应用程序仍然有可能发生崩溃,导致重复处理数据,数据库里就会出现重复记录。如果保存记录和偏移量可以在一个原子操作里完成,就可以避免出现上述情况。记录和偏移量要么都被成功提交,要么都不提交。如果记录是保存在数据库里而偏移量是提交到Kafka上,那么就无法实现原子操作，不过,如果在同一个事务里把记录和偏移量都写到数据库里，记录和偏移量要么都成功提交,要么都没有,然后重新处理记录就可以避免重复消费。  
问题是:如果偏移量是保存在数据库里而不是 Kafka 里,那么消费者在得到新分区时怎么知道该从哪里开始读取?这个时候可以使用 seek()方法。在消费者启动或分配到新分区时,可以使用 seek()方法查找保存在数据库里的偏移量并从偏移量消费。  
####优雅退出
如果确定要退出循环,需要通过另一个线程调用 consumer. wakeup()方法。consumer. wakeup()是消费者唯一一个可以从其他线程里安全调用的方法。  
调用 consumer. wakeup()可以退出 poll(),并抛出 WakeupException 异常。不需要处理 Wakeup Exception,因为它只是用于跳出循环的一种方式。不过,在退出线程之前调用 consumer.close()是很有必要的,它会提交任何还没有提交的东西,并向群组协调器发送消息,告知自己要离开群组,接下来就会触发再均衡,而不需要等待会话超时。
####反序列化
就是序列化过程的一个反向，原理和实现可以参考生产者端的实现，同样也可以自定义反序列化器。
####独立消费者 （了解）
一般我们说的消费者都属于消费者群组,都是分区被自动分配给群组里的消费者,在群组里新增或移除消费者时自动触发再均衡。    
不过有时候可能只需要一个消费者从一个主题的所有分区或者某个特定的分区读取数据。这个时候就不需要消费者群组和再均衡了,只需要把主题或者分区分配给消费者,然后开始读取消息并提交偏移量。  
这样的话,不需要订阅主题,取而代之的是为自己分配分区。一个消费者可以订阅主题(并加入消费者群组),或者为自己分配分区,但不能同时做这两件事情。   
```aidl
 //TODO 拿到主题的分区信息
List<PartitionInfo> partitionInfos= consumer.partitionsFor(SINGLE_CONSUMER_TOPIC);
//TODO 构造分区的List(这里全部都消费)
if(null!=partitionInfos){
    for(PartitionInfo partitionInfo:partitionInfos){
        topicPartitionList.add(new TopicPartition(partitionInfo.topic(),partitionInfo.partition()));
    }
}
//TODO 独立消费者需要消费哪些分区（列表）
consumer.assign(topicPartitionList);
```  
独立消费者相当于自己来分配分区，这样做的好处是自己控制，但是就没有动态的支持了，包括加入消费者（分区再均衡之类的），新增分区，这些都需要代码中去解决，所以一般情况下不推荐使用。  
