package sliu.service;

import sliu.domain.QueueInfo;

import java.util.List;

public interface QueueInfoService {
    int signIn(QueueInfo var1) throws Exception;

    int updateQueue(QueueInfo var1) throws Exception;

    int insertQueues(List<QueueInfo> var1) throws Exception;

    int insertQueue(QueueInfo var1) throws Exception;

    int insertNewQueue(QueueInfo var1) throws Exception;

    int updateMyQueueStatus(Integer var1, Boolean var2) throws Exception;

    QueueInfo selectQueueByName(String var1) throws Exception;

    List<QueueInfo> queryOtherQueueInfos();

    List<QueueInfo> queryAllQueueInfos();
}
