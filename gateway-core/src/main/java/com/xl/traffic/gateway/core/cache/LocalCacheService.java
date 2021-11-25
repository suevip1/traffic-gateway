package com.xl.traffic.gateway.core.cache;

import io.netty.channel.ChannelHandlerContext;

/**
 * 删除本地服务缓存
 *
 * @author: xl
 * @date: 2021/11/25
 **/
public interface LocalCacheService {


    void removeLocalCache(ChannelHandlerContext ctx);


}
