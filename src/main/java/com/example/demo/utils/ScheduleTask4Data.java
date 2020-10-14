package com.example.demo.utils;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;


/**
 * @Author: gyzhang
 * @Date: 2020/10/13 21:11
 */
@Data
@Configuration
@EnableScheduling
public class ScheduleTask4Data {

    private Double[] dataArr;

    /**
     * 定时任务，定时读取数据文件
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void task4Data() {
        this.dataArr = ReadTxtFile.getContent();
        System.out.println("定时任务执行完毕！" + new Date());
    }

}
