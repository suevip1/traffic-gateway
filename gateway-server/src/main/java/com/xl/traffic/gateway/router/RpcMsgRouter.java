package com.xl.traffic.gateway.router;


import com.xl.traffic.gateway.callback.BussinessCallback;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.consumer.RpcMsgConsumer;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.Protostuff;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.rpc.callback.Callback;
import com.xl.traffic.gateway.core.helper.AppHelper;
import com.xl.traffic.gateway.hystrix.downgrade.easy.EasyHystrixUtil;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class RpcMsgRouter {

    private static class InstanceHolder {
        public static final RpcMsgRouter instance = new RpcMsgRouter();
    }

    public static RpcMsgRouter getInstance() {
        return RpcMsgRouter.InstanceHolder.instance;
    }


    ISerialize iSerialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    /**
     * 发送消息给对应的业务节点,有回调的
     */
    public void sendAsync(RpcMsg rpcMsg) {
        /**获取cmd value*/
        String cmdValue = AppHelper.getInstance().cmdValue(rpcMsg.getCmd());
        String appNameValue = AppHelper.getInstance().appNameValue(rpcMsg.getAppName());
        String appGroupValue = AppHelper.getInstance().appGroupValue(rpcMsg.getGroup());
        /**权重 选择节点*/
        EasyHystrixUtil.invokeDowngrateMethodWithoutReturn(appGroupValue, appNameValue, cmdValue, () -> {
            log.info(cmdValue + "被降级！！！");
        }, () -> {
            /**执行业务*/
            NodePoolManager.getInstance().chooseRpcClient(appGroupValue).sendAsync(rpcMsg, new BussinessCallback(), 100);
        });
    }


    /**
     * 发送消息给对应的业务节点,同步消息
     */
    public RpcMsg sendSync(RpcMsg rpcMsg) {
        /**获取cmd value*/
        String cmdValue = AppHelper.getInstance().cmdValue(rpcMsg.getCmd());
        String appNameValue = AppHelper.getInstance().appNameValue(rpcMsg.getAppName());
        String appGroupValue = AppHelper.getInstance().appGroupValue(rpcMsg.getGroup());
        /**权重 选择节点*/
        RpcMsg result = EasyHystrixUtil.invokeDowngrateMethod(appGroupValue, appNameValue, cmdValue, () -> {
            log.info(cmdValue + "被降级！！！");
            String errorMsg = cmdValue + "被降级！！！";
            rpcMsg.setBody(iSerialize.serialize(errorMsg));
            return rpcMsg;
        }, () -> {
            RpcMsg callBussinessRpcMsg = null;
            try {
                /**执行call bussiness业务*/
                callBussinessRpcMsg = NodePoolManager.getInstance()
                        .chooseRpcClient(appGroupValue).sendSync(rpcMsg, 100);
            } catch (Exception ex) {
                log.error("call bussiness is error:{}", ex);
            }
            return callBussinessRpcMsg;
        });
        return result;
    }


}
