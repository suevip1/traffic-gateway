package com.xl.traffic.gateway.callback;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.rpc.callback.Callback;

/**
 * 业务回调类
 *
 * @author: xl
 * @date: 2021/6/30
 **/
public class BussinessCallback implements Callback<RpcMsg> {


    /**
     * 处理服务端返回的消息，用于做返回客户端，适合场景：长链接通讯
     *
     * @param result
     * @return: void
     * @author: xl
     * @date: 2021/6/30
     **/
    @Override
    public void handleResult(RpcMsg result) {
        /**获取路由信息，根据type做事件派发器将数据传给客户端*/


    }

    @Override
    public void handleError(Throwable error) {
        /**获取路由信息，将数据传给客户端*/

    }
}
