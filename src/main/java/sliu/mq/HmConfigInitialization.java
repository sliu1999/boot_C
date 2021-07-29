package sliu.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import sliu.config.HmConfigConfiguration;
import sliu.domain.QueueInfo;
import sliu.service.QueueInfoService;
import sliu.unit.ThreadOperate;

/**
 * 初始化类
 */
@Component
@Order(100)
public class HmConfigInitialization implements ApplicationRunner {




    //引用此类，进行了队列信息的初始化
    //交换机的创建，公共队列，私有队列创建，与交换机的绑定
    @Autowired
    private HmConfigConfiguration config;
    @Autowired
    private QueueConsumerFactory queueConsumerFactory;
    @Autowired
    private PublicQueueProducer publicQueueProducer;
    @Autowired
    private HmConfigDataInitService service;
    @Autowired
    private QueueInfoService queueInfoService;
    @Value("${spring.application.name}")
    private String appname;
    @Value("${server.port}")
    private String port;
    private final Logger logger = LoggerFactory.getLogger(HmConfigInitialization.class);

    public HmConfigInitialization() {
    }

    @Override
    public void run(ApplicationArguments args) {
        //创建私有队列和公共队列的监听
        this.createConsumer();
        //对私有队列做入库
        this.signInPrivateQueue();
    }

    private void createConsumer() {
        //新增监听私有队列，并设置消息的并发消费者数量为1
        this.queueConsumerFactory.getConsumer(this.config.getQueuePrivate().getName()).start();
        //新增监听公共队列，并设置消息的并发消费者数量为1
        this.queueConsumerFactory.getConsumer(this.config.getQueuePublic().getName()).start();
    }

    //项目启动时，对新注册的队列做入库，并同步基础信息

    public void signInPrivateQueue() {
        try {
            //查询自己库本条队列信息
            QueueInfo myQueueInfo = this.queueInfoService.selectQueueByName(this.appname);
            if (myQueueInfo == null) {
                //如果为null,说明是第一次注册，则新增此private队列信息
                myQueueInfo = new QueueInfo();
                myQueueInfo.setName(this.appname);
                myQueueInfo.setQueue(this.config.getQueuePrivate().getName());
                myQueueInfo.setTopic((String)this.config.getQueuePrivate().getTopic().get(0));
                myQueueInfo.setStatus(0);
                //对自己的队列做入库
                this.queueInfoService.insertNewQueue(myQueueInfo);
            } else {
                //不是第一次注册，则更新
                myQueueInfo.setName(this.appname);
                myQueueInfo.setQueue(this.config.getQueuePrivate().getName());
                myQueueInfo.setTopic((String)this.config.getQueuePrivate().getTopic().get(0));
                this.queueInfoService.updateQueue(myQueueInfo);
            }

            //发消息对私有队列做入库
            this.publicQueueProducer.signIn(myQueueInfo);
        } catch (Exception var2) {
            var2.printStackTrace();
            this.logger.error("私有队列注册信息发送失败！错误信息:{}", var2.getMessage());
        }

    }

    public void sendDataInit(QueueInfo queueInfo) {
        Integer hashCode = ThreadOperate.runSingleThread("data-init-" + queueInfo.getName(), new HmConfigDataInitRunable(this.service, queueInfo));
        ThreadOperate.removeThreadPool(hashCode);
    }
}
