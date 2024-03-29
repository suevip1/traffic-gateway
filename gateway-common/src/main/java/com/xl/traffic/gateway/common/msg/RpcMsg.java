package com.xl.traffic.gateway.common.msg;

import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;


/**
 * @Description: rpc消息
 * @Author: xl
 * @Date: 2021/6/22
 * 1.1 协议:
 __ __ __ __ __ __ __ __ __ ____ __ __ __ __ ____ __ ____ __ __ _____ __ __ ____ __ __ __ __ __ __ __ __
 * |              |              |            |           |           |           |                         |
 *         2              4            1             1           4           1             Uncertainty
 * |__ __ __ __ __|__ __ __ __ __|__ __ __ ___|__ __ __ __|__ __ __ __|__ __ __ __|_ __ __ __ __ __ __ __ __|
 * |              |              |            |           |           |           |                         |
 *        包尾        BodyLength       CMD        GROUP          ID       ZIP           CONTENT
 * |__ __ __ __ __|__ __ __ __ __|__ __ __ ___|__ __ __ __|__ __ __ __|__ __ __ __|__ __ __ ____ __ __ __ __|
 * */
@Data
public class RpcMsg implements Serializable {

    private byte cmd;//具体请求的命令

    private byte group;//请求组

    private byte appName;//请求app应用名称

    private long reqId;//请求ID

    private byte[] token;//请求token

    private byte[] body;//传输内容

    private byte zip; //是否支持压缩

    public RpcMsg(byte cmd, byte group, byte appName, long reqId, byte[] body) {
        this.cmd = cmd;
        this.group = group;
        this.appName = appName;
        this.reqId = reqId;
        this.body = body;
    }

    public RpcMsg(byte cmd, byte group, byte appName, long reqId, byte[] token, byte[] body) {
        this.cmd = cmd;
        this.group = group;
        this.appName = appName;
        this.reqId = reqId;
        this.token = token;
        this.body = body;
    }

    public RpcMsg(byte cmd, byte group, byte appName, long reqId, byte[] body, byte zip) {
        this.cmd = cmd;
        this.group = group;
        this.appName = appName;
        this.reqId = reqId;
        this.body = body;
        this.zip = zip;
    }


    public RpcMsg(byte cmd, byte group, byte appName, long reqId, byte[] token, byte[] body, byte zip) {
        this.cmd = cmd;
        this.group = group;
        this.appName = appName;
        this.reqId = reqId;
        this.token = token;
        this.body = body;
        this.zip = zip;
    }

    public RpcMsg() {
    }

    @Override
    public String toString() {
        return "RpcMsg{" +
                "cmd=" + cmd +
                ", reqId=" + reqId +
                ", body=" + Arrays.toString(body) +
                ", zip=" + zip +
                '}';
    }
}
