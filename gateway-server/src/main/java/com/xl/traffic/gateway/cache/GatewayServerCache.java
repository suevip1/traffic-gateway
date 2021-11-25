package com.xl.traffic.gateway.cache;

import com.xl.traffic.gateway.common.msg.RpcMsg;
import com.xl.traffic.gateway.common.utils.AddressUtils;
import com.xl.traffic.gateway.core.cache.LocalCacheService;
import com.xl.traffic.gateway.core.dto.RouterDTO;
import com.xl.traffic.gateway.core.enums.MsgAppNameType;
import com.xl.traffic.gateway.core.enums.MsgCMDType;
import com.xl.traffic.gateway.core.enums.MsgGroupType;
import com.xl.traffic.gateway.core.enums.SerializeType;
import com.xl.traffic.gateway.core.serialize.ISerialize;
import com.xl.traffic.gateway.core.serialize.SerializeFactory;
import com.xl.traffic.gateway.core.thread.ThreadPoolExecutorUtil;
import com.xl.traffic.gateway.core.utils.AttributeKeys;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import com.xl.traffic.gateway.rpc.pool.NodePoolManager;
import io.netty.channel.ChannelHandlerContext;

public class GatewayServerCache implements LocalCacheService {

    ISerialize serialize = SerializeFactory.getInstance().getISerialize(SerializeType.protobuf);

    @Override
    public void removeLocalCache(ChannelHandlerContext ctx) {

        ThreadPoolExecutorUtil.getGateway_Login_Out_Pool().execute(() -> {
            //获取设备id
            String deviceIdChannelId = ctx.channel().attr(AttributeKeys.DEVICE_ID).get();
            ConnectionManager.getInstance().delConnection(deviceIdChannelId);
            String userId = ctx.channel().attr(AttributeKeys.USER_ID).get();
            String deviceId = ctx.channel().attr(AttributeKeys.SOURCE_DEVICE_ID).get();
            RouterDTO routerDTO = new RouterDTO(userId, AddressUtils.getInnetIp(), deviceId);

            RpcMsg rpcMsg = new RpcMsg(MsgCMDType.LOGIN_OUT_CMD.getType()
                    , MsgGroupType.ROUTER.getType(), MsgAppNameType.ROUTER.getType(), 0, serialize.serialize(routerDTO));
            /**删除用户ip关系*/
            NodePoolManager.getInstance().chooseRpcClient(GatewayConstants.ROUTER_GROUP).sendAsync(rpcMsg);
        });

    }
}
