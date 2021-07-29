package sliu.service.impl;

import com.alibaba.fastjson.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sliu.mapper.InitializeMapper;
import sliu.mq.PrivateQueueProducer;
import sliu.service.InitializeService;

@Service
@Transactional(
        rollbackFor = {Exception.class}
)
public class InitializeServiceImpl implements InitializeService {
    private static Logger logger = LoggerFactory.getLogger(InitializeServiceImpl.class);
    @Autowired
    private InitializeMapper initializeMapper;
    @Autowired
    private PrivateQueueProducer privateQueueProducer;
    private Integer size = 100;

    public InitializeServiceImpl() {
    }

    /**
     * 给新注册的队列发送表数据信息
     * @param topic 新注册的消息队列topic
     * @param tableName 要发送的表
     * @return
     * @throws Exception
     */
    @Override
    public int sendInitializeInfos(String topic, String tableName) throws Exception {
        HashMap<String, String> hashMap = new HashMap(16);
        hashMap.put("tableName", tableName);
        //先查下这张表的数据量
        int count = this.initializeMapper.queryInfoCount(hashMap);
        if (count > 0) {
            //如果表里有数据
            //进行循环 i每次加100
            for(int i = 0; i < count; i += 100) {
                HashMap<String, Object> map = new HashMap(16);
                map.put("num", i);
                map.put("size", this.size);
                map.put("tableName", tableName);
                //查这个表说要列名
                List<HashMap> columns = this.initializeMapper.selectColumnsInfo(tableName);
                //查表数据
                List<HashMap> infos = this.initializeMapper.queryInfos(map);
                HashMap<String, Object> data = new HashMap(16);
                List<List<JSONObject>> list = new ArrayList();
                List<String> keys = new ArrayList();
                Iterator var12 = columns.iterator();

                while(var12.hasNext()) {
                    HashMap column = (HashMap)var12.next();
                    keys.add(column.get("Field").toString());
                }

                infos.forEach((info) -> {
                    List<JSONObject> values = new ArrayList();
                    Iterator var4 = columns.iterator();

                    while(var4.hasNext()) {
                        HashMap column = (HashMap)var4.next();
                        JSONObject value = new JSONObject();
                        value.put("type", column.get("Type"));
                        value.put("value", info.get(column.get("Field")));
                        values.add(value);
                    }

                    list.add(values);
                });
                data.put("keys", keys);
                data.put("list", list);
                data.put("tableName", tableName);
                //发送表数据给新注册的队列
                this.privateQueueProducer.sendInitializeInfos(topic, data);
            }
        }

        return 0;
    }

    /**
     * 新注册的消息队列接收表信息，并保存
     * @param data
     * @return
     * @throws Exception
     */
    @Override
    public int saveInitializeInfos(HashMap<String, Object> data) throws Exception {
        this.initializeMapper.insertInfos(data);
        return 0;
    }

    @Override
    public int backupTable(String tableName) throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String datetime = simpleDateFormat.format(new Date());
        HashMap<String, String> params = new HashMap(16);
        params.put("tableName", tableName);
        params.put("datetime", datetime);
        return this.initializeMapper.createBackupTable(params);
    }

    @Override
    public int clearTable(String tableName) throws Exception {
        this.backupTable(tableName);
        HashMap<String, String> params = new HashMap(16);
        params.put("tableName", tableName);
        return this.initializeMapper.clearTable(params);
    }
}
