package com.xl.traffic.gateway.core.token;

import com.xl.traffic.gateway.core.helper.ZKConfigHelper;
import com.xl.traffic.gateway.core.utils.GatewayConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;


/**
 * token工具类
 *
 * @author: xl
 * @date: 2021/7/13
 **/
@Slf4j
public class Token {

    /**
     * key: value
     */
    public static final String KEY_VALUE = "value";

    /**
     * key: 有效期 (时间戳)
     */
    public static final String KEY_EFF = "eff";

    /**
     * token 过期时间默认为20分钟
     */
    public static final long EXPIRE_TIME = GatewayConstants.TOKEN_TIMEOUT;

    /**
     * token 秘钥
     */
    public static final String KEYT = ZKConfigHelper.getInstance().getGatewayCommonConfig().getTokenSecurityKeyt();


    /**
     * 根据指定值创建 jwt-token
     *
     * @param value 要保存的值
     * @return jwt-token
     */
    public static String createToken(Object value) {
        // 计算eff有效期
        long eff = EXPIRE_TIME + System.currentTimeMillis();

        // 在这里你可以使用官方提供的claim方法构建载荷，也可以使用setPayload自定义载荷，但是两者不可一起使用
        JwtBuilder builder = Jwts.builder()
                // .setHeaderParam("typ", "JWT")
                .claim(KEY_VALUE, value)
                .claim(KEY_EFF, eff)
                .signWith(SignatureAlgorithm.HS256, KEYT.getBytes());
        // 生成jwt-token
        return builder.compact();
    }

    /**
     * 从一个 jwt-token 解析出载荷
     *
     * @param jwtToken JwtToken值
     * @return Claims对象
     */
    public static Claims parseToken(String jwtToken) {
        // 解析出载荷
        Claims claims = Jwts.parser()
                .setSigningKey(KEYT.getBytes())
                .parseClaimsJws(jwtToken).getBody();
        // 返回
        return claims;
    }

    /**
     * 从一个 jwt-token 解析出载荷, 并取出数据
     *
     * @param jwtToken JwtToken值
     * @return 值
     */
    public static Object getValue(String jwtToken) {
        // 取出数据
        Claims claims = parseToken(jwtToken);

        // 验证是否超时
        Long eff = claims.get(KEY_EFF, Long.class);
        if ((eff == null || eff < System.currentTimeMillis())) {
            log.info("token 已过期！");
            return null;
        }

        // 获取数据
        return claims.get(KEY_VALUE);
    }

    /**
     * 从一个 jwt-token 解析出载荷, 并取出其剩余有效期
     *
     * @param jwtToken JwtToken值
     * @return 值
     */
    public static long getTimeout(String jwtToken) {
        // 取出数据
        Claims claims = parseToken(jwtToken);

        // 验证是否超时
        Long eff = claims.get(KEY_EFF, Long.class);

        // 已经超时
        if (eff == null || eff < System.currentTimeMillis()) {
            return -1;
        }
        // 计算timeout
        return (eff - System.currentTimeMillis()) / 1000;
    }


}
