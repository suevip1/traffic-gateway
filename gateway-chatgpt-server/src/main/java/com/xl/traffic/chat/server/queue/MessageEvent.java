package com.xl.traffic.chat.server.queue;


import lombok.Data;

@Data
public class MessageEvent<T> {


    private T msg;


}
