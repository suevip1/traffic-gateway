package com.xl.traffic.gateway.core.serialize;

import com.xl.traffic.gateway.core.enums.SerializeType;

import java.util.HashMap;
import java.util.Map;

public class SerializeFactory {


    private static Map<SerializeType, ISerialize> serializeMap = new HashMap<>();

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
        serializeMap.put(SerializeType.protobuf, new Protostuff());
    }

    public ISerialize getISerialize(SerializeType mqType) {
        return serializeMap.get(mqType);
    }


}
