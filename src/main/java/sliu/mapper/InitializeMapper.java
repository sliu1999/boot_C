package sliu.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface InitializeMapper {
    int queryInfoCount(Map var1);

    List<HashMap> queryInfos(Map var1);

    List<HashMap> selectColumnsInfo(@Param("tableName") String var1);

    int insertInfos(Map var1);

    int createBackupTable(Map var1);

    int clearTable(Map var1);
}
