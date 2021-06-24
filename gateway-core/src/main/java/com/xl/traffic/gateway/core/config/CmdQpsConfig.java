package com.xl.traffic.gateway.core.config;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class CmdQpsConfig {
    private Map<String, Integer> cmdQpsMap = new HashMap<>();
}
