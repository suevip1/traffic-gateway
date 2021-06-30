package com.xl.traffic.gateway.rpc.handler;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.utils.SnowflakeIdWorker;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class RpcKeepaliveHandler extends ChannelDuplexHandler {


    private RpcMsg heartCmd;


    /**
     * 构造心跳消息
     */
    public RpcKeepaliveHandler() {
        heartCmd = new RpcMsg();
        heartCmd.setReqId(SnowflakeIdWorker.getInstance().nextId().intValue());
        heartCmd.setCmd((byte) MsgCMDType.HEAT_CMD.getType());
    }

    /**
     * @Description: 心跳检测<>读写全部超时</>
     * @Param: [ctx, evt]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //心跳配置
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                ctx.writeAndFlush(heartCmd);
            }
        }
//        if (evt instanceof IdleStateEvent) {
//            ctx.close();
//        }
//        super.userEventTriggered(ctx, evt);
    }
}
