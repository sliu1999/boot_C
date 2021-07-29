package sliu.service.impl;

import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sliu.domain.QueryQueueInfoDTO;
import sliu.domain.QueueInfo;
import sliu.mapper.QueueInfoMapper;
import sliu.mq.HmConfigInitialization;
import sliu.mq.PrivateQueueProducer;
import sliu.mq.PublicQueueProducer;
import sliu.service.QueueInfoService;

@Service
@Transactional(
        rollbackFor = {Exception.class}
)
public class QueueInfoServiceImpl implements QueueInfoService {
    private static final Logger logger = LoggerFactory.getLogger(QueueInfoServiceImpl.class);
    @Value("${spring.application.name}")
    private String appname;
    @Autowired
    private QueueInfoMapper queueInfoMapper;
    @Autowired
    private PrivateQueueProducer privateQueueProducer;
    @Autowired
    private PublicQueueProducer publicQueueProducer;
    @Autowired
    private HmConfigInitialization hmConfigInitialization;
    private static Integer RETRY_SIGN_IN = 0;
    private static Integer RETRY_SIGN_IN_MAX = 2;

    public QueueInfoServiceImpl() {
    }

    /**
     * 私有队列注册入库接口
     * 监听公共队列的signIn消息的实现类，只会被消费一次，设置了并发消费者为1
     * @param queueInfo
     * @return
     * @throws Exception
     */
    @Override
    public int signIn(QueueInfo queueInfo) throws Exception {
        //判断要入库的这个私有队列是不是本工程的队列
        Boolean isMyself = this.appname.equals(queueInfo.getName());
        //如果是本工程
        if (isMyself) {
            if (RETRY_SIGN_IN < RETRY_SIGN_IN_MAX) {
                //如果是前两次注册，则把注册的消息发送到公共队列
                this.publicQueueProducer.signIn(queueInfo);
                Integer var9 = RETRY_SIGN_IN;
                RETRY_SIGN_IN = RETRY_SIGN_IN + 1;
            } else {
                //如果超过3次注册入库，则直接修改此数据状态status=1,并且不发送消息通知其他队列
                this.updateMyQueueStatus(1, false);
            }

            return 1;

        } else if (!this.validateSignIn(queueInfo)) {
            //如果不是本工程队列，并且队列信息不完整，则返回
            logger.error("队列注册传入参数有误！参数:{}", queueInfo);
            return 2;
        } else {
            //如果不是本工程的私有队列注册入库信息
            //先查下这条队列是否已经录入本库
            QueueInfo localQueueInfo = this.queueInfoMapper.selectById(queueInfo.getId());
            QueryQueueInfoDTO queryQueueInfoDTO;
            List queueInfos;
            Iterator var6;
            QueueInfo info;
            Boolean need;

            if (localQueueInfo == null) {
                //如果此队列以前没有在本库做入库，则入库
                this.queueInfoMapper.insertQueue(queueInfo);
                queryQueueInfoDTO = new QueryQueueInfoDTO();
                queryQueueInfoDTO.addExcludeName(queueInfo.getName());
                //查找除本次入库的私有队列外，其他的队列信息
                queueInfos = this.queueInfoMapper.selectAll(queryQueueInfoDTO.toMap());
                var6 = queueInfos.iterator();

                while(var6.hasNext()) {
                    //循环发送除本队列外的其他私有队列，让其对本次新注册的队列做入库
                    info = (QueueInfo)var6.next();
                    need = !info.getName().equals(this.appname);
                    if (need) {
                        this.privateQueueProducer.insertQueueInfo(info.getTopic(), queueInfo);
                    }
                }
                //发送消息给本次注册的消息队列，让其初始化除本次注册的其他队列信息
                this.privateQueueProducer.initQueuesInfo(queueInfo.getTopic(), queueInfos);
            } else {
                //如果此队列以前在本库做过入库，则更新此次注册的队列信息
                this.queueInfoMapper.updateQueue(queueInfo);
                queryQueueInfoDTO = new QueryQueueInfoDTO();
                queryQueueInfoDTO.addExcludeName(queueInfo.getName());
                //查找除本次入库的私有队列外，其他的队列信息
                queueInfos = this.queueInfoMapper.selectAll(queryQueueInfoDTO.toMap());
                var6 = queueInfos.iterator();

                while(var6.hasNext()) {
                    //循环发送除本队列外的其他私有队列，让其对本次新注册的队列做更新
                    info = (QueueInfo)var6.next();
                    need = !info.getName().equals(this.appname);
                    if (need) {
                        this.privateQueueProducer.updateQueueInfo(info.getTopic(), queueInfo);
                    }
                }
            }

            if (queueInfo.getStatus() == 0) {
                //如果本次新注册的队列status=0  如果自己库以前没有此队列信息，则status为0
                //开启新单例线程，
                this.hmConfigInitialization.sendDataInit(queueInfo);
            }

            return 0;
        }
    }

    private Boolean validateSignIn(QueueInfo queueInfo) {
        if (queueInfo == null) {
            return false;
        } else if (queueInfo.getId() == null) {
            return false;
        } else if (queueInfo.getName() == null) {
            return false;
        } else if (queueInfo.getQueue() == null) {
            return false;
        } else {
            return queueInfo.getTopic() == null ? false : true;
        }
    }

    @Override
    public int updateQueue(QueueInfo queueInfo) throws Exception {
        this.queueInfoMapper.updateQueue(queueInfo);
        return 0;
    }


    @Override
    public int insertQueues(List<QueueInfo> queues) throws Exception {
        Iterator var2 = queues.iterator();

        while(var2.hasNext()) {
            QueueInfo queueInfo = (QueueInfo)var2.next();
            this.queueInfoMapper.insertQueue(queueInfo);
        }

        return 0;
    }

    /**
     * 监听到来自其他私有队列的有关私有队列注册入库的消息
     * 接受到之后，执行队列入库
     * @return
     * @throws Exception
     */
    @Override
    public int insertQueue(QueueInfo queueInfo) throws Exception {
        this.queueInfoMapper.insertQueue(queueInfo);
        return 0;
    }

    @Override
    public int insertNewQueue(QueueInfo record) throws Exception {
        this.queueInfoMapper.insertNewQueue(record);
        return 0;
    }

    /**
     * 新注册的消息队列接收消息
     * @param status
     * @param needMq
     * @return
     * @throws Exception
     */
    @Override
    public int updateMyQueueStatus(Integer status, Boolean needMq) throws Exception {
        QueueInfo myQueueInfo = this.queueInfoMapper.selectByName(this.appname);
        if (myQueueInfo == null) {
            logger.error("修改队列状态失败！未查询到本服务的队列信息！appname:{}", this.appname);
            return 2;
        } else {
            myQueueInfo.setStatus(status);
            this.queueInfoMapper.updateQueue(myQueueInfo);
            if (needMq) {
                QueryQueueInfoDTO queryQueueInfoDTO = new QueryQueueInfoDTO();
                queryQueueInfoDTO.addExcludeName(this.appname);
                List<QueueInfo> queueInfos = this.queueInfoMapper.selectAll(queryQueueInfoDTO.toMap());
                Iterator var6 = queueInfos.iterator();

                while(var6.hasNext()) {
                    QueueInfo info = (QueueInfo)var6.next();
                    this.privateQueueProducer.updateQueueInfo(info.getTopic(), myQueueInfo);
                }
            }

            return 0;
        }
    }

    @Override
    public QueueInfo selectQueueByName(String name) throws Exception {
        return this.queueInfoMapper.selectByName(name);
    }

    @Override
    public List<QueueInfo> queryOtherQueueInfos() {
        QueryQueueInfoDTO queryQueueInfoDTO = new QueryQueueInfoDTO();
        queryQueueInfoDTO.addExcludeName(this.appname);
        return this.queueInfoMapper.selectAll(queryQueueInfoDTO.toMap());
    }

    @Override
    public List<QueueInfo> queryAllQueueInfos() {
        QueryQueueInfoDTO queryQueueInfoDTO = new QueryQueueInfoDTO();
        return this.queueInfoMapper.selectAll(queryQueueInfoDTO.toMap());
    }
}
