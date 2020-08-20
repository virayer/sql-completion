package org.virayer.sqlcompletion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.virayer.sqlcompletion.dao.DBMapper;
import org.virayer.sqlcompletion.entity.ClassInfo;
import org.virayer.sqlcompletion.entity.DDLInfo;
import org.virayer.sqlcompletion.entity.FieldInfo;
import org.virayer.sqlcompletion.util.FileUtil;
import org.virayer.sqlcompletion.util.JarPathUtil;
import org.virayer.sqlcompletion.util.TableParseUtil;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Virayer
 * @date 2020/7/15
 */
@Component
@Order(100)
public class Bootstrap implements ApplicationRunner {

    @Autowired
    DBMapper dbMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {

//        exportSQLBySelectDBName("zigbeedata");

        importSQLDdlToDB("zigbeedata");
        System.out.println("importSQLDdlToDB finish");

    }

    private void importSQLDdlToDB(String dbName) {
        String path = JarPathUtil.getPath() + File.separator + "sql_ddl";
        String dbPath = path + File.separator + dbName;

        //查询数据库当前DDL信息
        List<DDLInfo> dataBaseCurrentDDLs = queryDDLByDBName(dbName);


        //判断是否存在DB路径
        File file = new File(dbPath);
        if (file.exists()) {
            //如果存在DB路径，对路径下所有.sql文件进行扫描读取DLL
            List<File> files = Arrays.stream(Objects.requireNonNull(file.listFiles()))
                    .filter(d -> d.getName().endsWith(".sql"))
                    .collect(Collectors.toList());

            for (File sqlFile : files) {
                //不带扩展名的名称
                String fileName = sqlFile.getName().substring(0, sqlFile.getName().lastIndexOf("."));
                String fileData = FileUtil.readFileByChars(sqlFile.getAbsolutePath());

                Optional<DDLInfo> optional = dataBaseCurrentDDLs.stream().filter(d -> d.getTableName().equals(fileName)).findFirst();
                if (optional.isPresent()) {
                    //数据库找到对应DDL，进行检查DDL，进行新增字段等操作。
                    try {

                        ClassInfo currentDBClassInfo = TableParseUtil.processTableIntoClassInfo(optional.get().getTableDDL(), false);
                        ClassInfo fileClassInfo = TableParseUtil.processTableIntoClassInfo(fileData, false);
                        for (int i = 0; i < fileClassInfo.getFieldList().size(); i++) {
                            FieldInfo fieldInfo = fileClassInfo.getFieldList().get(i);
                            Optional<FieldInfo> fieldInfoOptional = currentDBClassInfo.getFieldList().stream().filter(d -> d.getFieldName().equals(fieldInfo.getFieldName())).findFirst();
                            if (!fieldInfoOptional.isPresent()) {
                                //如果数据库里没有字段，添加该字段
                                String orderInfo = "FIRST";
                                if (i > 0) {
                                    FieldInfo fieldInfoLast = fileClassInfo.getFieldList().get(i - 1);
                                    orderInfo = "AFTER " + fieldInfoLast.getFieldName();
                                }
                                String addColumSql = String.format("ALTER TABLE %s.`%s` ADD COLUMN %s %s;", dbName, fileName, fieldInfo.getFieldType(), orderInfo);
                                dbMapper.ExecuteNonQuery(addColumSql);
                                System.out.println(String.format("add table column '%s' by %s.`%s`", fieldInfo.getFieldName(), dbName, fileClassInfo.getTableName()));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //数据库没有找到对应DDL，进行新增表操作
                    String createSql = fileData.replace(String.format("`%s`", fileName), String.format("%s.`%s`", dbName, fileName));
                    dbMapper.ExecuteNonQuery(createSql);
                    System.out.println(String.format("create table %s.`%s`", dbName, fileName));
                }
            }


        }

    }


    private void exportSQLBySelectDBName(String dbName) throws IOException {
        String path = JarPathUtil.getPath() + File.separator + "sql_ddl";
        String dbPath = path + File.separator + dbName;
        FileUtil.createDirectory(dbPath);

        List<DDLInfo> ddlInfos = queryDDLByDBName(dbName);
        for (DDLInfo ddlInfo : ddlInfos) {
            String tableName = ddlInfo.getTableName();
            String createTableDdl = ddlInfo.getTableDDL();
            String tablePath = dbPath + File.separator + tableName + ".sql";
            FileUtil.writeFileByFileWriter(tablePath, createTableDdl);
        }

        System.out.println(String.format("(%s) processed, already save to %s", ddlInfos.size(), dbPath));
    }

    /**
     * 根据数据库名获取数据库下所有DDL对象信息
     *
     * @param dbName
     * @return
     */
    private List<DDLInfo> queryDDLByDBName(String dbName) {
        List<DDLInfo> tableDDLList = new ArrayList<>();
        List<String> tableNamesByDBName = dbMapper.getTableNamesByDBName(dbName);
        tableNamesByDBName.forEach(tableNameItem -> {
            DDLInfo tableDDLByDBNameAndTableName = dbMapper.getTableDDLByDBNameAndTableName(dbName, tableNameItem);
            tableDDLList.add(tableDDLByDBNameAndTableName);
        });
        return tableDDLList;
    }
}
