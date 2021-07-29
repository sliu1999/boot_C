package sliu.mq;

import com.alibaba.fastjson.JSONObject;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sliu.config.HmConfigConfiguration;
import sliu.domain.QueueInfo;
import sliu.service.QueueInfoService;
import sliu.unit.OperationMessage;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Component
public class PrivateQueueProducer {
    @Autowired
    private AmqpTemplate rabbitTemplate;
    @Autowired
    HmConfigConfiguration config;
    @Autowired
    private QueueInfoService queueInfoService;
    private List<QueueInfo> queueInfos;

    public PrivateQueueProducer() {
    }

    public void insertQueueInfo(String topic, QueueInfo queueInfo) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{QueueInfo.class};
        Object[] argValues = new Object[]{queueInfo};
        OperationMessage operationMessage = new OperationMessage("queueInfoServiceImpl.insertQueue", argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), topic, message);
    }

    public void initQueuesInfo(String topic, List<QueueInfo> queueInfoList) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{QueueInfo.class};
        Object[] argValues = new Object[]{queueInfoList};
        OperationMessage operationMessage = new OperationMessage("queueInfoServiceImpl.insertQueues", argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), topic, message);
    }

    public void updateQueueInfo(String topic, QueueInfo queueInfo) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{QueueInfo.class};
        Object[] argValues = new Object[]{queueInfo};
        OperationMessage operationMessage = new OperationMessage("queueInfoServiceImpl.updateQueue", argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), topic, message);
    }

    public void completeDataInit(String topic) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{Integer.class, Boolean.class};
        Object[] argValues = new Object[]{1, true};
        OperationMessage operationMessage = new OperationMessage("queueInfoServiceImpl.updateMyQueueStatus", argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), topic, message);
    }

    private void sendOther(MessageProperties messageProperties, Class[] argTypes, Object[] argValues, String method) throws Exception {
        OperationMessage operationMessage = new OperationMessage(method, argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.queueInfos = this.queueInfoService.queryOtherQueueInfos();
        Iterator var7 = this.queueInfos.iterator();

        while(var7.hasNext()) {
            QueueInfo info = (QueueInfo)var7.next();
            this.rabbitTemplate.convertAndSend(this.config.getExchange(), info.getTopic(), message);
        }

    }

    private void sendOne(MessageProperties messageProperties, Class[] argTypes, Object[] argValues, String method, String topic) throws Exception {
        OperationMessage operationMessage = new OperationMessage(method, argTypes, argValues);
        Message message = new Message(JSONObject.toJSONString(operationMessage).getBytes("UTF-8"), messageProperties);
        this.rabbitTemplate.convertAndSend(this.config.getExchange(), topic, message);
    }


    public void sendInitializeInfos(String topic, HashMap data) throws Exception {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setPriority(10);
        Class[] argTypes = new Class[]{HashMap.class};
        Object[] argValues = new Object[]{data};
        this.sendOne(messageProperties, argTypes, argValues, "initializeServiceImpl.saveInitializeInfos", topic);
    }


}
