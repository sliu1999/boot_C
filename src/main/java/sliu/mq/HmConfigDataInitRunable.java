package sliu.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sliu.domain.QueueInfo;

public class HmConfigDataInitRunable implements Runnable {

    private HmConfigDataInitService service;
    private QueueInfo queueInfo;

    public HmConfigDataInitRunable(HmConfigDataInitService service, QueueInfo queueInfo) {
        this.service = service;
        this.queueInfo = queueInfo;
    }

    @Override
    public void run() {
        this.service.start(this.queueInfo);
    }
}
