package org.virayer.sqlcompletion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.virayer.sqlcompletion.dao.DBMapper;

@SpringBootApplication
public class SqlCompletionApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqlCompletionApplication.class, args);

        //初始化开始程序
//        initApplication();
    }

//    private static void initApplication() {
//        String databaseName = "oauth";
//
//    }

}
