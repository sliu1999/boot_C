package sliu.domain;

import java.util.List;

public class ExchangeProperty {
    private String name;
    private List<QueueProperty> queue;

    public ExchangeProperty() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<QueueProperty> getQueue() {
        return this.queue;
    }

    public void setQueue(List<QueueProperty> queue) {
        this.queue = queue;
    }
}
