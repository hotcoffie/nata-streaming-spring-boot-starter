### 说明

网上关于nats-streaming和springboot的资源非常匮乏，为了工作需要自制了nats-streaming的springboot starter，支持参数配置和注解式的消息订阅

### 使用方法

1.打包到maven仓库，引用依赖：

```xml
<dependency>
    <groupId>com.github.hotcoffie</groupId>
    <artifactId>nats-streaming-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

2.开启Nats支持：

```java
@SpringBootApplication
@EnableNatsStreaming
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
```

3.application.yml

```yaml
spring:
  nats:
    streaming:
      client-id: natsServer
      urls: nats://[user:passwd@]host:port
```

更多配置详见`org.springframework.nats.properties.NatsStreamingSubProperties`和`org.springframework.nats.properties.NatsStreamingProperties`

4.主题订阅

```java
@NatsStreamingSubscribe(subscribe = "testSubscribe", queue = "testQueue")
public void doSth(Message msg) {
    System.out.println(msg.toString());
}
```



代码参考： [wanlinus/nats-streaming-spring](https://github.com/wanlinus/nats-streaming-spring)
