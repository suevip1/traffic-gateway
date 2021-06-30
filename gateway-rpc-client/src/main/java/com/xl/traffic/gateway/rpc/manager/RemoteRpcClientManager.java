package com.xl.traffic.gateway.rpc.manager;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.callback.CallFuture;
import com.xl.traffic.gateway.core.callback.Callback;
import com.xl.traffic.gateway.core.exception.RPCException;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import com.xl.traffic.gateway.core.utils.ThreadPoolUtils;
import com.xl.traffic.gateway.rpc.client.RpcClient;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import com.xl.traffic.gateway.rpc.statistics.StatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


/**
 * 远程rpc 调用
 */
public class RemoteRpcClientManager {


    /**
     * 请求rpc超时时间，默认是60s
     */
    public static final int RpcTimeout = 60 * 1000;

    static {

        //todo 定制化
//        if (ServerConfig.containsKey(ServerConfig.KEY_RPC_CONNECT_TIMEOUT)) {
//            RpcTimeout = ServerConfig.getInt(ServerConfig.KEY_RPC_CONNECT_TIMEOUT);
//        } else {
//            RpcTimeout = 60 * 1000;
//        }
    }

    private static Logger logger = LoggerFactory.getLogger(RemoteRpcClientManager.class);

    private static class InstanceHolder {
        public static final RemoteRpcClientManager instance = new RemoteRpcClientManager();
    }

    public static RemoteRpcClientManager getInstance() {
        return InstanceHolder.instance;
    }


    /**
     * 同步发送,阻塞,
     * <p>
     * 访问延迟大将会导致线程挂起太久,CPU无法跑满,而解决方法只有新建更多线程,性能不好,
     * <p>
     * 如作为主请求路由RPC强烈不建议用
     *
     * @throws InterruptedException
     * @throws RPCException
     */
    public byte[] sendSync(String group, byte[] content) throws RPCException, InterruptedException {
        return sendAsync(group, content, RpcTimeout)
                .get(RpcTimeout, TimeUnit.MILLISECONDS);
    }


    /**
     * 异步Future
     */
    public CallFuture<byte[]> sendAsync(String group, byte[] content, int timeout) {
        CallFuture<byte[]> callFuture = CallFuture.newInstance();
        sendAsync(group, content, callFuture, timeout);
        return callFuture;
    }


    /**
     * 异步回调,nio
     */
    public boolean sendAsync(String group, byte[] content, Callback<byte[]> callback) {
        return sendAsync(group, content, callback, RpcTimeout);
    }


    /**
     * 异步,nio ，不带回调的
     */

    public boolean sendAsync(String group, byte[] content) {

        try {
            //选择rpcClient
            RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(group);
            if (rpcClient != null) {
                RpcMsg request = new RpcMsg();
                request.setReqId(SnowflakeIdWorker.getInstance().nextId());
                request.setBody(content);
                rpcClient.sendAsync(request);
                return true;
            } else {
                logger.error("can no choose pool:" + group);
                return false;
            }
        } catch (Exception ex) {
            logger.error("sendAsync group:{} error: {} ", group, ex);
        }
        return false;
    }

    /**
     * 异步回调,nio
     */

    public boolean sendAsync(String group, byte[] content, Callback<byte[]> callback, int rpcTimeOut) {

        try {
            //选择rpcClient
            RpcClient rpcClient = NodePoolManager.getInstance().chooseRpcClient(group);
            if (rpcClient != null) {
                RpcMsg request = new RpcMsg();
                request.setReqId(SnowflakeIdWorker.getInstance().nextId());
                request.setBody(content);
                rpcClient.sendAsync(request, new AsyncCallback(callback, rpcClient.getIpPort(), request, group), rpcTimeOut);
                return true;
            } else {
                logger.error("can no choose pool:" + group);
                callback.handleError(new RPCException("can no choose pool:" + group));
                return false;
            }
        } catch (Exception ex) {
            callback.handleError(new RPCException("sendAsync error:", ex));

        }
        return false;
    }


    /**
     * 包装异步回调,统计服务端调用信息，做中介者模式
     */
    public static class AsyncCallback implements Callback<RpcMsg> {
        /**
         * 客户端自己的结果回调类
         */
        private Callback<byte[]> resultCallBack;
        private String ipport;
        /**
         * 请求模块
         */
        private String action;

        public AsyncCallback(Callback<byte[]> resultCallBack, String ipport, RpcMsg message, String action) {
            this.resultCallBack = resultCallBack;
            this.ipport = ipport;
            this.action = action;
            /**统计*/
            StatisticsManager.getInstance().start(ipport, action, message);

        }

        @Override
        public void handleResult(RpcMsg result) {
            resultCallBack.handleResult(result.getBody());
            /**异步执行统计调用量*/
            ThreadPoolUtils.getInstance().getExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    /**统计*/
                    StatisticsManager.getInstance().success(ipport, action, result);
                }
            });
        }

        @Override
        public void handleError(Throwable error) {
            resultCallBack.handleError(error);
            /**统计*/
            StatisticsManager.getInstance().fail(ipport, action, error);
        }
    }


}
