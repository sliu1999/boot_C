package sliu.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sliu.domain.QueueInfo;
import sliu.service.InitializeService;

@Component
public class HmConfigDataInitService {
    private static final Logger logger = LoggerFactory.getLogger(HmConfigDataInitService.class);
    @Autowired
    private PrivateQueueProducer privateQueueProducer;

    @Autowired
    private InitializeService initializeService;

    public HmConfigDataInitService() {
    }

    /**
     * 新注册的队列的信息初始化
     * 这个方法送signIn方法开辟出来，只会被执行一次
     * @param queueInfo 新注册的消息队列
     */
    public void start(QueueInfo queueInfo) {
        try {
            String[] tableNames = new String[]{"tb_sys_department", "tb_sys_staff", "tb_sys_staff_r_department", "tb_sys_staff_icon_card", "tb_sys_staff_icon_photo", "tb_sys_user"};

            //给新注册的队列发消息和表数据
            for(int i = 0; i < tableNames.length; ++i) {
                this.initializeService.sendInitializeInfos(queueInfo.getTopic(), tableNames[i]);
            }

            this.privateQueueProducer.completeDataInit(queueInfo.getTopic());
        } catch (Exception var4) {
            var4.printStackTrace();
            logger.error("初始化数据发送失败！错误信息:{}", var4.getMessage());
        }

    }
}
