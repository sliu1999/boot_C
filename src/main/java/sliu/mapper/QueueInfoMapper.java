package sliu.mapper;

import sliu.domain.QueueInfo;

import java.util.List;
import java.util.Map;

public interface QueueInfoMapper {
    QueueInfo selectByName(String var1);

    QueueInfo selectById(String var1);

    int insertNewQueue(QueueInfo var1);

    int insertQueue(QueueInfo var1);

    int updateQueue(QueueInfo var1);

    List<QueueInfo> selectAll(Map var1);
}
