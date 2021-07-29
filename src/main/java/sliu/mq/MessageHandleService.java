package sliu.mq;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sliu.unit.*;


/**
 * 消息监听
 */
@Component
public class MessageHandleService implements ChannelAwareMessageListener {
    private Logger logger = LoggerFactory.getLogger(MessageHandleService.class);
    private String ACTION_SPLIT = "\\.";

    public MessageHandleService() {
    }

    /**
     * 消息监听的实际方法
     * @param message
     * @param channel
     * @throws Exception
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        ApplicationContext wac = SpringManager.getApplicationContext();
        byte[] body = message.getBody();
        this.logger.info("接收到消息:" + new String(body));
        JSONObject jsonObject = null;

        try {
            jsonObject = JSONObject.parseObject(new String(body));
            if (jsonObject == null) {
                throw new Exception("消息体为空！");
            } else {
                MessageUtil.Type type = MessageUtil.Type.valueOf(jsonObject.getString("type"));
                String action;
                Class[] argTypes;
                Object[] argValues;
                Object obj;
                Method method;
                MethodArgs methodArgs;
                switch(type) {
                    case OPERATION:
                        OperationMessage operationMessage = (OperationMessage)JSONObject.parseObject(new String(body), OperationMessage.class);
                        action = operationMessage.getAction();
                        methodArgs = this.modifyArgs(operationMessage.getArgTypes(), operationMessage.getArgValues());
                        argTypes = methodArgs.getArgTypes();
                        argValues = methodArgs.getArgValues();
                        obj = wac.getBean(action.split(this.ACTION_SPLIT)[0]);
                        method = obj.getClass().getDeclaredMethod(action.split(this.ACTION_SPLIT)[1], argTypes);
                        method.invoke(obj, argValues);
                        break;
                    case QUERY:
                        QueryMessage queryMessage = (QueryMessage)JSONObject.parseObject(new String(body), QueryMessage.class);
                        action = queryMessage.getAction();
                        methodArgs = this.modifyArgs(queryMessage.getArgTypes(), queryMessage.getArgValues());
                        argTypes = methodArgs.getArgTypes();
                        argValues = methodArgs.getArgValues();
                        Class replyType = queryMessage.getReplyType();
                        obj = wac.getBean(action.split(this.ACTION_SPLIT)[0]);
                        method = obj.getClass().getDeclaredMethod(action.split(this.ACTION_SPLIT)[1], argTypes);
                        Object replyValue = method.invoke(obj, argValues);
                        ReplyMessage replyMessage = new ReplyMessage(replyType, replyValue);
                        String replyQueue = message.getMessageProperties().getReplyTo();
                        AMQP.BasicProperties replyProps = (new AMQP.BasicProperties.Builder()).correlationId(message.getMessageProperties().getCorrelationId()).build();
                        channel.basicPublish("", replyQueue, replyProps, JSONObject.toJSONString(replyMessage).getBytes("UTF-8"));
                        break;
                    default:
                        throw new Exception("消息类型错误！");
                }

                this.logger.info("消息消费成功！");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception var20) {
            this.logger.error("消息消费失败！错误信息:{}", var20.getMessage());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            throw var20;
        }
    }

    private MethodArgs modifyArgs(Class[] argTypes, Object[] argValues) {
        MethodArgs methodArgs = new MethodArgs(argTypes);

        for(int i = 0; i < argValues.length; ++i) {
            String jsonStr;
            if (argValues[i] != null && argValues[i].getClass() == JSONObject.class) {
                jsonStr = ((JSONObject)argValues[i]).toJSONString();
                methodArgs.addValue(i, JSONObject.parseObject(jsonStr, argTypes[i]));
            } else if (argValues[i] != null && argValues[i].getClass() == JSONArray.class) {
                jsonStr = ((JSONArray)argValues[i]).toJSONString();
                methodArgs.addValue(i, JSONArray.parseArray(jsonStr, argTypes[i]));
                argTypes[i] = List.class;
            } else {
                methodArgs.addValue(i, argValues[i]);
            }
        }

        methodArgs.setArgTypes(argTypes);
        return methodArgs;
    }
}

