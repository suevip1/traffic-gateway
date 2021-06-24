package com.xl.traffic.gateway.rpc.process;

import com.xl.traffic.gateway.common.msg.RpcMsg;

/**
 * cmd 业务处理
 *
 * @author: xl
 * @date: 2021/6/24
 **/
public interface ICmdProcess {


    byte[] execute(RpcMsg rpcMsg);

}
