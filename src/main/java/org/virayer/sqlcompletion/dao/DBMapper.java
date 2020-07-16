package org.virayer.sqlcompletion.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.virayer.sqlcompletion.entity.DDLInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Virayer
 * @date 2020/7/15
 */
@Mapper
@Repository
public interface DBMapper {

    List<String> getTableNamesByDBName(@Param("dbName")String dbName);

    DDLInfo getTableDDLByDBNameAndTableName(@Param("dbName")String dbName, @Param("tableName")String tableName);

    int ExecuteNonQuery(@Param("sql") String sql);
}
