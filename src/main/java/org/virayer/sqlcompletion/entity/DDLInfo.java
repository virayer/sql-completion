package org.virayer.sqlcompletion.entity;

import lombok.Data;

/**
 * @author Virayer
 * @date 2020/7/15
 */
@Data
public class DDLInfo {
    private String tableName;
    private String tableDDL;
}
