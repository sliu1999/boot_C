package sliu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import sliu.domain.QueueProperty;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Configuration
@ConfigurationProperties(
        prefix = "spring.hmconfig"
)
@Component
public class HmConfigConfiguration {
    private static Logger logger = LoggerFactory.getLogger(HmConfigConfiguration.class);
    private QueueProperty queuePublic;
    private QueueProperty queuePrivate;
    private String exchange;


    @Autowired
    private AmqpAdmin rabbitAdmin;


    private Queue queuePublicObj;
    private Queue queuePrivateObj;

    public HmConfigConfiguration() {
    }

    @PostConstruct
    public void init() {
        logger.info("HmConfigConfiguration 初始化……");
        TopicExchange topicExchange = new TopicExchange(this.exchange);
        //定义交换机
        this.rabbitAdmin.declareExchange(topicExchange);
        Map<String, Object> args = new HashMap(16);
        args.put("x-max-priority", 10);
        this.queuePublicObj = new Queue(this.queuePublic.getName(), true, false, false, args);
        //定义公共队列，并设置队列优先级
        this.rabbitAdmin.declareQueue(this.queuePublicObj);
        Iterator var3 = this.queuePublic.getTopic().iterator();

        String str;
        Binding binding;
        //公共队列绑定交换机
        while(var3.hasNext()) {
            str = (String)var3.next();
            binding = BindingBuilder.bind(this.queuePublicObj).to(topicExchange).with(str);
            this.rabbitAdmin.declareBinding(binding);
        }

        this.queuePrivateObj = new Queue(this.queuePrivate.getName(), true, false, false, args);
        //定义私有队列，并设置队列优先级
        this.rabbitAdmin.declareQueue(this.queuePrivateObj);
        var3 = this.queuePrivate.getTopic().iterator();

        //私有队列绑定交换机
        while(var3.hasNext()) {
            str = (String)var3.next();
            binding = BindingBuilder.bind(this.queuePrivateObj).to(topicExchange).with(str);
            this.rabbitAdmin.declareBinding(binding);
        }

    }

    public QueueProperty getQueuePublic() {
        return this.queuePublic;
    }

    public void setQueuePublic(QueueProperty queuePublic) {
        this.queuePublic = queuePublic;
    }

    public QueueProperty getQueuePrivate() {
        return this.queuePrivate;
    }

    public void setQueuePrivate(QueueProperty queuePrivate) {
        this.queuePrivate = queuePrivate;
    }

    public String getExchange() {
        return this.exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Queue getQueuePublicObj() {
        return this.queuePublicObj;
    }

    public Queue getQueuePrivateObj() {
        return this.queuePrivateObj;
    }
}
