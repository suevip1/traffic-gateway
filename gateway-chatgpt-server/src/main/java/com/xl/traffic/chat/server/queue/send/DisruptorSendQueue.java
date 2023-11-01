package com.xl.traffic.chat.server.queue.send;

import com.xl.traffic.chat.server.message.ImMessage;
import com.xl.traffic.chat.server.queue.MessageEvent;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xuliang
 * @version 1.0
 * @project traffic-gateway
 * @description
 * @date 2023/10/26 16:47:06
 */
public class DisruptorSendQueue {

    private  RingBuffer<MessageEvent<ImMessage>> ringBuffer;
    public DisruptorSendQueue() {
        init();

    }
    public  void produceData(ImMessage msg) {
        long sequence = ringBuffer.next(); // 获得下一个Event槽的下标
        try {
            // 给Event填充数据
            MessageEvent event = ringBuffer.get(sequence);
            event.setMsg(msg);

        } finally {
            // 发布Event，激活观察者去消费， 将sequence传递给该消费者
            // 注意，最后的 ringBuffer.publish() 方法必须包含在 finally 中以确保必须得到调用；如果某个请求的 sequence 未被提交，将会堵塞后续的发布操作或者其它的 producer。
            ringBuffer.publish(sequence);
        }
    }

    public  void init(){
        // 指定 ring buffer字节大小，必需为2的N次方(能将求模运算转为位运算提高效率 )，否则影响性能
        int bufferSize = 1024 * 1024;
        //固定线程数
        ExecutorService executor = Executors.newFixedThreadPool(1);
        EventFactory<MessageEvent<ImMessage>> factory = new EventFactory<MessageEvent<ImMessage>>() {
            @Override
            public MessageEvent newInstance() {
                return new MessageEvent();
            }
        };
        // 创建ringBuffer
        ringBuffer = RingBuffer.create(ProducerType.SINGLE, factory, bufferSize,
                new YieldingWaitStrategy());
        SequenceBarrier barriers = ringBuffer.newBarrier();
        ChatGptSendConsumer[] consumers = new ChatGptSendConsumer[1];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i] = new ChatGptSendConsumer();
        }
        WorkerPool<MessageEvent<ImMessage>> workerPool = new WorkerPool<MessageEvent<ImMessage>>(ringBuffer, barriers,
                new EventExceptionHandler(), consumers);
        ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(executor);
    }

    @Slf4j
    public static class EventExceptionHandler implements ExceptionHandler {

        @Override
        public void handleEventException(Throwable ex, long sequence, Object event) {
            log.error("handleEventException：" + ex);
        }

        @Override
        public void handleOnShutdownException(Throwable ex) {
            log.error("handleEventException：" + ex);
        }

        @Override
        public void handleOnStartException(Throwable ex) {
            log.error("handleOnStartException：" + ex);
        }

    }


}
