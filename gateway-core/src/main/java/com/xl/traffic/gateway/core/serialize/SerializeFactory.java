package com.xl.traffic.gateway.core.serialize;

import com.xl.traffic.gateway.core.enums.SerializeType;

import java.util.HashMap;
import java.util.Map;

public class SerializeFactory {


    private static Map<SerializeType, ISerialize> mqClientMap = new HashMap<>();

    private static class InstanceHolder {
        public static final SerializeFactory instance = new SerializeFactory();
    }

    public static SerializeFactory getInstance() {
        return InstanceHolder.instance;
    }

    public SerializeFactory() {
        init();
    }

    /**
     * 初始化操作
     */
    public void init() {
        mqClientMap.put(SerializeType.protobuf, new Protostuff());
    }

    public ISerialize getISerialize(SerializeType mqType) {
        return mqClientMap.get(mqType);
    }


}
