package sliu.unit;

public class ReplyMessage {
    private MessageUtil.Type type;
    private Class replyType;
    private Object replyValue;

    public ReplyMessage() {
    }

    public ReplyMessage(Class replyType, Object replyValue) {
        this.type = MessageUtil.Type.REPLY;
        this.replyType = replyType;
        this.replyValue = replyValue;
    }

    public MessageUtil.Type getType() {
        return this.type;
    }

    public void setType(MessageUtil.Type type) {
        this.type = type;
    }

    public Class getReplyType() {
        return this.replyType;
    }

    public void setReplyType(Class replyType) {
        this.replyType = replyType;
    }

    public Object getReplyValue() {
        return this.replyValue;
    }

    public void setReplyValue(Object replyValue) {
        this.replyValue = replyValue;
    }
}
