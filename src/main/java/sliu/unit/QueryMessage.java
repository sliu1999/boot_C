package sliu.unit;

import sliu.unit.MessageUtil;

public class QueryMessage {
    private MessageUtil.Type type;
    private String action;
    private Class[] argTypes;
    private Object[] argValues;
    private Class replyType;

    public QueryMessage() {
    }

    public QueryMessage(String action, Class[] argTypes, Object[] argValues, Class replyType) {
        this.type = MessageUtil.Type.QUERY;
        this.action = action;
        this.argTypes = argTypes;
        this.argValues = argValues;
        this.replyType = replyType;
    }

    public MessageUtil.Type getType() {
        return this.type;
    }

    public void setType(MessageUtil.Type type) {
        this.type = type;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Class[] getArgTypes() {
        return this.argTypes;
    }

    public void setArgTypes(Class[] argTypes) {
        this.argTypes = argTypes;
    }

    public Object[] getArgValues() {
        return this.argValues;
    }

    public void setArgValues(Object[] argValues) {
        this.argValues = argValues;
    }

    public Class getReplyType() {
        return this.replyType;
    }

    public void setReplyType(Class replyType) {
        this.replyType = replyType;
    }
}
