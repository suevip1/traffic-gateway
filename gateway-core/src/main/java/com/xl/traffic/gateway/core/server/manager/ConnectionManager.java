package com.xl.traffic.gateway.core.server.manager;

import com.xl.traffic.gateway.core.cache.CaffineCacheUtil;
import com.xl.traffic.gateway.core.server.connection.Connection;
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
     * @param channelId  channelId
     * @param connection 连接
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void addConnection(String channelId, Connection connection) {
        CaffineCacheUtil.getConnectionCache().put(channelId, connection);
    }

    /**
     * 根据设备id获取Connection
     *
     * @param channelId
     * @return: io.netty.channel.Channel
     * @author: xl
     * @date: 2021/7/13
     **/
    public Connection getConnection(String channelId) {
        return CaffineCacheUtil.getConnectionCache().getIfPresent(channelId);
    }


    /**
     * 根据channelId删除 Connection
     *
     * @param channelId
     * @return: void
     * @author: xl
     * @date: 2021/7/13
     **/
    public void delConnection(String channelId) {
        CaffineCacheUtil.getConnectionCache().invalidate(channelId);
    }
}
