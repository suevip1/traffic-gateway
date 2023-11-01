package com.xl.traffic.gateway.router;


import com.xl.traffic.gateway.callback.BussinessCallback;
import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.node.ServerNodeInfo;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
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
        String to = "";/**这里举个取模的例子*/
        /**从当前appGroup当中根据uid取模获取节点*/
        ServerNodeInfo serverNodeInfo = NodePoolManager.getInstance().chooseRpcNode(appGroupValue, to);
        /**校验当前节点的降级点是否降级*/
        /**如果降级,为当前组的当前节点的降级点做降级，熔断时长*/
        /**如果服务处理失败,重新选择负载,直播间,群,私聊等*/
        /**权重 选择节点*/
        EasyHystrixUtil.invokeDowngrateMethodWithoutReturn(serverNodeInfo.getIp() + "-" + appGroupValue, appNameValue, cmdValue, () -> {
            log.info("当前节点:" + serverNodeInfo.getIp() + "appGroup:" + appGroupValue + cmdValue + "被降级！！！进行一段时间的熔断");
        }, () -> {
            /**执行业务，异步发送，带回调的*/
            NodePoolManager.getInstance().chooseModelRpcClient(appGroupValue,to).sendAsync(rpcMsg, new BussinessCallback(), 100);
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
            RpcMsg callResult = null;
            try {
                /**执行call bussiness业务*/
                callResult = NodePoolManager.getInstance()
                        .chooseRpcClient(appGroupValue).sendSync(rpcMsg, 100);
            } catch (Exception ex) {
                log.error("call bussiness is error:{}", ex);
            }
            return callResult;
        });
        return result;
    }


}
