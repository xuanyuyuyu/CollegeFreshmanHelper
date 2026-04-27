package com.example.collegefreshmanhelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({
        "com.example.collegefreshmanhelper.admin.mapper",
        "com.example.collegefreshmanhelper.user.mapper",
        "com.example.collegefreshmanhelper.forum.mapper"
})
public class CollegeFreshmanHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollegeFreshmanHelperApplication.class, args);
    }

}
