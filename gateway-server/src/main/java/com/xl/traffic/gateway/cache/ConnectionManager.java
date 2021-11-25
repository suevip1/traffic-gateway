package com.xl.traffic.gateway.cache;

import com.xl.traffic.gateway.core.cache.CaffineCacheUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

/**
 * 客户端连接管理
 * Structure:
 * __ __ __ ____ __ ____ __ __ __ __ __ __ __ __ __ __ __
 * |                           |                          |
 * deviceId1__channelId1             Channel1
 * |__ __ __ __ __ __ __ __ __ |__ __ __ __ __ __ __ __ __|
 * |                           |                          |
 * deviceId2__channelId2    	     Channel2
 * |__ __ __ __ __ __ __ __ __ |__ __ __ __ __ __ __ __ __|
 * |                           |                          |
 * deviceId3__channelId3    	     Channel3
 * |__ __ __ __ __ __ __ __ __ |__ __ __ __ __ __ __ __ __|
 *
 * @author xl
 * @version 2021年7月13日
 */
@Slf4j
public class ConnectionManager {
    private static class InstanceHolder {
        private static ConnectionManager instance = new ConnectionManager();
    }

    public static ConnectionManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 保存连接
     *
     * @param deviceChannelId deviceChannelId
     * @param channel         连接
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void addConnection(String deviceChannelId, Channel channel) {
        CaffineCacheUtil.getConnectionCache().put(deviceChannelId, channel);
    }

    /**
     * 根据channelId获取Connection(客户端要求带过来用户的设备channelId)
     *
     * @param deviceChannelId
     * @return: io.netty.channel.Channel
     * @author: xl
     * @date: 2021/7/13
     **/
    public Channel getConnection(String deviceChannelId) {
        return CaffineCacheUtil.getConnectionCache().getIfPresent(deviceChannelId);
    }

    /**
     * 根据deviceId删除 Connection
     *
     * @param deviceChannelId
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void delConnection(String deviceChannelId) {
        CaffineCacheUtil.getConnectionCache().invalidate(deviceChannelId);
    }
}
