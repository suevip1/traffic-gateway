package com.xl.traffic.chat.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 14:34:18
 */
@SpringBootApplication
public class ChatGptServerApplication  implements CommandLineRunner {



    public static void main(String[] args) {
        SpringApplication.run(ChatGptServerApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {

    }
}
