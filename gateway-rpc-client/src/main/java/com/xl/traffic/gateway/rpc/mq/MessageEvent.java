package com.xl.traffic.gateway.rpc.mq;


import lombok.Data;

@Data
public class MessageEvent<T> {


    private T msg;


}
