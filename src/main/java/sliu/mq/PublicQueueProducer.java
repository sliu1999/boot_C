package sliu.mq;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sliu.config.HmConfigConfiguration;
import sliu.domain.QueueInfo;
import sliu.unit.OperationMessage;

@Component
public class PublicQueueProducer {
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    HmConfigConfiguration config;

    public PublicQueueProducer() {
    }

    /**
     * 发送消息给交换机上的public公共队列
     * 消息内容为 私有队列注册signIn
     * @param myQueueInfo
     * @throws Exception
     */
    public void signIn(QueueInfo myQueueInfo) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{QueueInfo.class};
        Object[] argValues = new Object[]{myQueueInfo};
        OperationMessage operationMessage = new OperationMessage("queueInfoServiceImpl.signIn", argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), (String)this.config.getQueuePublic().getTopic().get(0), message);
    }
}
