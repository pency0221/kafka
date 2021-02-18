##与Spirng集成   
首先引入Spirng kafka依赖：
```aidl
<dependency>
   <groupId>org.springframework.kafka</groupId>
   <artifactId>spring-kafka</artifactId>
   <version>2.2.0.RELEASE</version>
</dependency>
```

###生产者相关：
生产者相关配置组件依赖关系:  
`kafkaTemplate->producerFactory->producerProperties(bootstrap.servers地址、key和value序列化器...)`   

发送消息使用template即可: `kafkaTemplate.send("kafka-spring-topic",message);  `

kafkaTemplate也可以配置发送结果回调监听：  
```aidl
<!-- TODO 指定 发送监听器bean -->
<property name="producerListener" ref="sendListener">
```
`public class SendListener implements ProducerListener `

###消费者相关：
消费者相关配置依赖关系：  
```aidl
    messageListenerContainer(->consumerFactory、ContainerProperties)
    consumerFactory(->consumerPropertie)
    ContainerProperties(->Topics,messageListener消费者)
```

消费者手动提交偏移量：
1. consumerProperties中关闭自动enable.auto.commit
```aidl
<!--TODO 关闭自动提交-->
<entry key="enable.auto.commit" value="false"/>
```  
2. containerProperties中设置手动监听器内提交  
 ```aidl
 <!-- TODO 消费者自行手动提交偏移量 -->
<property name="ackMode" value="MANUAL_IMMEDIATE">
 ```  
3. 消费者实现AcknowledgingMessageListener 而非MessageListener（为了拿到acknowledgment对象）  
`public class KafkaConsumerAck implements AcknowledgingMessageListener<String,String>   `  
实现的消费消息方法onMessage内调用 acknowledgment.acknowledge()手动提交。   
   

具体见本次提交代码。