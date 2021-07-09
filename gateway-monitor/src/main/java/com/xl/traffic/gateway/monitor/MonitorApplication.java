package com.xl.traffic.gateway.monitor;

import com.xl.traffic.gateway.monitor.start.MonitorStart;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MonitorApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(MonitorApplication.class, args);
    }


    @Override
    public void run(String... args) throws Exception {
        MonitorStart.getInstance().start();
        MonitorStart.getInstance().stop();
    }
}
