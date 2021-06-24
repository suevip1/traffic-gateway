package com.xl.traffic.gateway.demo.cmd;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.rpc.process.ICmdProcess;

public class LoginCmd implements ICmdProcess {


    @Override
    public byte[] execute(RpcMsg rpcMsg) {
        return new byte[0];
    }
}
