package com.xl.traffic.gateway.core.server.connection;

/**
 * connection接收器，往连接中添加handler
 */
public abstract class AbstractConnectionInitializer implements ConnectionInitializer {

    /**
     * @Description: 初始化连接
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    @Override
    public void initConnection(ConnectionFacade connectionFacade) {
        addDDOSHandlers(connectionFacade);
        addTimeoutHandlers(connectionFacade);
        addTcpHandlers(connectionFacade);
        addProtocolHandlers(connectionFacade);
        addBizHandlers(connectionFacade);
    }

    /**
     * @Description: 添加DDOS防攻击(
     * 思路 ：
     *      1 ， 识别来源ip
     *      2 ， 在一段时间内 ， 超出频次限制拉进黑名单
     *      3 ， 强制断开该来源ip的连接
     *      4 ， 黑名单智能安全校验
     *      5 ， 一段时间内不活跃的连接 进行主动关闭)
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    abstract public void addDDOSHandlers(ConnectionFacade connectionFacade);

    /**
     * @Description: 添加心跳超时检测机制
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    abstract public void addTimeoutHandlers(ConnectionFacade connectionFacade);

    /**
     * @Description: 添加限流等tcp 控制 ssl认证等
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    abstract public void addTcpHandlers(ConnectionFacade connectionFacade);

    /**
     * @Description: 添加协议处理器
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    abstract public void addProtocolHandlers(ConnectionFacade connectionFacade);

    /**
     * @Description: 添加业务处理器
     * @Param: [connectionFacade]
     * @return: void
     * @Author: xl
     * @Date: 2021/6/21
     **/
    abstract public void addBizHandlers(ConnectionFacade connectionFacade);
}
