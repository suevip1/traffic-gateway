package com.xl.traffic.gateway;


import com.xl.traffic.gateway.start.GatewayServerStart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class GatewayApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //服务开启
        GatewayServerStart.getInstance().start();
        //添加钩子，用于服务停止
        GatewayServerStart.getInstance().stop();
    }
}
