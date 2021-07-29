package sliu.unit;

public class MethodArgs {
    private Class[] argTypes;
    private Object[] argValues;

    public MethodArgs() {
    }

    public MethodArgs(Class[] argTypes) {
        this.argTypes = argTypes;
        this.argValues = new Object[argTypes.length];
    }

    public void addValue(int index, Object value) {
        if (this.argValues.length > index) {
            this.argValues[index] = value;
        }

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
}
