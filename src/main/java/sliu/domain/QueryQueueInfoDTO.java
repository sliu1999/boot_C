package sliu.domain;

import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryQueueInfoDTO {
    private List<String> excludeNames;

    public QueryQueueInfoDTO() {
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public HashMap<String, Object> toMap() {
        return (HashMap)JSONObject.parseObject(JSONObject.toJSONString(this), HashMap.class);
    }

    public List<String> getExcludeNames() {
        return this.excludeNames;
    }

    public void setExcludeNames(List<String> excludeNames) {
        this.excludeNames = excludeNames;
    }

    public void addExcludeName(String name) {
        if (this.excludeNames == null) {
            this.excludeNames = new ArrayList();
        }

        this.excludeNames.add(name);
    }
}
