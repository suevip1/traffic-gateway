package com.xl.traffic.gateway.admin;

import com.xl.traffic.gateway.admin.start.AdminClientStart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayAdminApplication  implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(GatewayAdminApplication.class,args);
    }

    @Override
    public void run(String... args) throws Exception {
        AdminClientStart.getInstance().start();
        AdminClientStart.getInstance().stop();
    }
}
