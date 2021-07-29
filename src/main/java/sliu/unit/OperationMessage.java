package sliu.unit;

import sliu.unit.MessageUtil;

public class OperationMessage {
    private MessageUtil.Type type;
    private String action;
    private Class[] argTypes;
    private Object[] argValues;

    public OperationMessage() {
    }

    public OperationMessage(String action, Class[] argTypes, Object[] argValues) {
        this.type = MessageUtil.Type.OPERATION;
        this.action = action;
        this.argTypes = argTypes;
        this.argValues = argValues;
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

    public MessageUtil.Type getType() {
        return this.type;
    }

    public void setType(MessageUtil.Type type) {
        this.type = type;
    }
}
