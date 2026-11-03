package com.example.gamecompanion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动入口。
 *
 * 运行方式：
 * 1. 在 IntelliJ IDEA 中直接右键运行本类的 main 方法；
 * 2. 或在项目根目录执行命令：mvn spring-boot:run
 *
 * 启动成功后，浏览器访问 http://localhost:8080 即可打开登录页。
 */
@SpringBootApplication
public class GameCompanionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameCompanionApplication.class, args);
        System.out.println("========================================================");
        System.out.println(" 游戏陪玩订单与评价平台 启动成功！");
        System.out.println(" 请访问：http://localhost:8080");
        System.out.println("========================================================");
    }
}
