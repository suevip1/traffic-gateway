package com.xl.traffic.gateway.client.server.handler;

/**
 * 业务端实现该接口
 *
 * @author: xl
 * @date: 2021/9/7
 **/
public interface RpcProcesserService<T> {

    /**
     * 接口方法
     *
     * @param params
     * @return: java.lang.Object
     * @author: xl
     * @date: 2021/9/7
     **/
    Object execute(T params);

}
