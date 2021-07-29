package sliu.domain;

import java.util.List;

public class QueueProperty {
    private String name;
    private List<String> topic;

    public QueueProperty() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTopic() {
        return this.topic;
    }

    public void setTopic(List<String> topic) {
        this.topic = topic;
    }
}
