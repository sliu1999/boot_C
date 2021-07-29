package sliu.service;

import java.util.HashMap;

public interface InitializeService {
    int sendInitializeInfos(String var1, String var2) throws Exception;

    int saveInitializeInfos(HashMap<String, Object> var1) throws Exception;

    int backupTable(String var1) throws Exception;

    int clearTable(String var1) throws Exception;
}
