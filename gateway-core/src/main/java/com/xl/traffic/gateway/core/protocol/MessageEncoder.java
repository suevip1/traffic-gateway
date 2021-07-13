package com.xl.traffic.gateway.core.protocol;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.core.helper.ZKConfigHelper;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.core.utils.DESCipher;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.core.zip.IZip;
import com.xl.traffic.gateway.core.zip.Zip;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xl
 * @date 2018年11月22日 上午11:55:38
 * <p>
 * 适用短连接/追求效率+比较可靠的长连接 包头长度+特定包尾 一旦丢包必须关闭重连,因为后面数据会错乱
 * <p>
 * 长度(4)包id(4)版本号(1)压缩类型(1)消息类型(1)内容(n)包尾(2)
 * 发送 编码
 */
@Slf4j
public class MessageEncoder extends MessageToByteEncoder<RpcMsg> {

    public final static byte[] BYTE_END = new byte[]{'\r', '\n'};


    @Override
    public void encode(ChannelHandlerContext ctx, RpcMsg in, ByteBuf out) throws Exception {


        byte[] content = in.getBody();
        if (content != null) {
            //校验是否需要加密
            if (ZKConfigHelper.getInstance().getGatewayCommonConfig().isSecurity()) {
                String sessionKey = ctx.channel().attr(AttributeKeys.SESSION_KEY).get();
                content = DESCipher.encrypt(content, sessionKey);
            }
            //判断是否需要压缩
            IZip iZip = Zip.get(in.getZip());
            if (iZip != null) content = iZip.compress(content);
            out.writeInt(content.length + GatewayConstants.MSG_LENGTH);
        } else {
            out.writeInt(GatewayConstants.MSG_LENGTH);
        }

        out.writeByte(in.getCmd());
        out.writeByte(in.getGroup());

        /**公共参数*/
        out.writeLong(in.getReqId());
        out.writeByte(in.getZip());

        //ver=0编码逻辑,后续更新通信版本需要区分处理逻辑
        out.writeByte(in.getZip());
        if (content != null) {
            out.writeBytes(content);
        }
        out.writeBytes(BYTE_END);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("MessageEncoder is error:{}", cause);

    }

}
