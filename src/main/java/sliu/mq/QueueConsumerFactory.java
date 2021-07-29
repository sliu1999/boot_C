package sliu.mq;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 队列消费者工厂
 */
@Component
public class QueueConsumerFactory {
    @Autowired
    private ConnectionFactory connectionFactory;
    @Autowired
    private MessageHandleService messageHandleService;
    private Integer defaultPrefetchCount = 1;
    private Integer defaultConcurrentConsumers = 1;

    public QueueConsumerFactory() {
    }

    public SimpleMessageListenerContainer getConsumer(String queueName) {
        return this.getConsumer(queueName, this.defaultPrefetchCount, this.defaultConcurrentConsumers);
    }

    /**
     * 简单消息监听容器
     * @param queueName
     * @param prefetchCount
     * @param concurrentConsumers
     * @return
     */
    public SimpleMessageListenerContainer getConsumer(String queueName, Integer prefetchCount, Integer concurrentConsumers) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(this.connectionFactory);
        container.setQueueNames(new String[]{queueName}); // 监听多个queue，运行时添加
        container.setExposeListenerChannel(true); //设置监听外露
        container.setPrefetchCount(prefetchCount);//告诉代理一次请求多少条消息过来
        container.setConcurrentConsumers(concurrentConsumers);// 设置并发消费者数量 1，及只支持被一个消费者消费，消息不会被重复消费，公共队列的消息被多个工程监听，但只能被消费一次
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);// 设置自确认模式为手动
        container.setMessageListener(this.messageHandleService); //设置监听
        return container;
    }
}
