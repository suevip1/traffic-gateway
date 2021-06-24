package com.xl.traffic.gateway.router;

import com.xl.traffic.gateway.router.start.RouterStart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayRouterApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(GatewayRouterApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        RouterStart.getInstance().start();
        RouterStart.getInstance().stop();
    }
}
