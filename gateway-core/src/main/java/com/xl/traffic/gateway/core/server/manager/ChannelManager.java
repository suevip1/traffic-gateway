package com.xl.traffic.gateway.core.server.manager;

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
public class ChannelManager {
    private static class InstanceHolder {
        private static ChannelManager instance = new ChannelManager();
    }

    public static ChannelManager getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * 保存channel
     *
     * @param deviceId 设备id
     * @param channel  通道channel
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void addChannel(String deviceId, Channel channel) {
        CaffineCacheUtil.getChannelCache().put(deviceId, channel);
    }

    /**
     * 根据设备id获取channel
     *
     * @param deviceId
     * @return: io.netty.channel.Channel
     * @author: xl
     * @date: 2021/7/13
     **/
    public Channel getChannel(String deviceId) {
        return CaffineCacheUtil.getChannelCache().getIfPresent(deviceId);
    }


    /**
     * 根据deviceID删除 channel
     *
     * @param deviceId
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void delChannel(String deviceId) {
        CaffineCacheUtil.getChannelCache().invalidate(deviceId);
    }
}
