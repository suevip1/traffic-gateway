package com.xl.traffic.gateway.hystrix.aspect;

import com.xl.traffic.gateway.core.exception.DowngrateException;
import com.xl.traffic.gateway.hystrix.DowngradeClient;
import com.xl.traffic.gateway.hystrix.XLDowngrateClientFactory;
import com.xl.traffic.gateway.hystrix.annotation.DowngrateMethod;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 降级注解切面
 *
 * @author: xl
 * @date: 2021/6/28
 **/
@Aspect
@Slf4j
public class DowngrateAspect {


    @Pointcut("@annotation(com.xl.traffic.gateway.hystrix.annotation.DowngrateMethod)")
    public void pointcut() {

    }


    @Around("pointcut()")
    public Object invoke(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Class<?> targetClass = pjp.getTarget().getClass();

        Method originMethod = getDeclaredMethod(targetClass, signature.getName(),
                signature.getMethod().getParameterTypes());
        if (originMethod == null) {
            throw new IllegalStateException(
                    "获取类方法失败，class：" + targetClass.getName() + ", method:" + signature.getMethod().getName());
        }


        DowngrateMethod downgrateMethodAnnotation = originMethod.getAnnotation(DowngrateMethod.class);
        if (downgrateMethodAnnotation == null) {
            throw new IllegalStateException("这不应该发生，请联系管理员！！");
        }

        DowngradeClient client = XLDowngrateClientFactory.getDowngrateClient();
        if (client == null) {
            return pjp.proceed();
        }

        String point = downgrateMethodAnnotation.point();
        try {
            /**如果需要降级，那么直接抛异常，不执行业务方法*/
            if (client.shouldDowngrade(point)) {
                throw new DowngrateException(point);
            }
            /**执行业务方法*/
            return pjp.proceed();
        } catch (DowngrateException ex) {
            Object downgradeReturnValue = null;
            /**优先判断是否被fallback 处理*/
            if (!StringUtils.isEmpty(downgrateMethodAnnotation.fallback())) {
                downgradeReturnValue = handlerFallBack(pjp, ex);
                if (null != downgradeReturnValue) {
                    return downgradeReturnValue;
                }
            }
            //todo 再判断是否被 sds-admin 降级处理
            /** 如果都没有处理，使用默认策略抛出异常*/
            ex.setPoint(point);
            throw ex;
        } catch (Throwable throwable) {
            /**这里统计异常量*/
            client.exceptionSign(point, throwable);
            throw throwable;
        } finally {
            /**释放资源*/
            client.downgradeFinally(point);
        }
    }


    /**
     * 处理自定义fallback异常
     *
     * @param pjp
     * @param ex
     * @return: java.lang.Object
     * @author: xl
     * @date: 2021/6/28
     **/
    private Object handlerFallBack(ProceedingJoinPoint pjp, DowngrateException ex) {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Class<?> targetClass = pjp.getTarget().getClass();
        Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
        Object[] args = pjp.getArgs();
        Method originMethod = this.getDeclaredMethod(targetClass, signature.getName(), parameterTypes);
        // 上面已经判断过，不会发生 NPE
        DowngrateMethod downgrateMethod = originMethod.getAnnotation(DowngrateMethod.class);
        /**获取降级方法名称*/
        String fallbackMethodName = downgrateMethod.fallback();
        // 优先查找带异常的方法
        Method fallbackMethod = null;
        Class<?>[] newParameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length + 1);
        newParameterTypes[newParameterTypes.length - 1] = ex.getClass();
        Object[] newArgs = Arrays.copyOf(args, args.length + 1);
        newArgs[newArgs.length - 1] = ex;
        fallbackMethod = this.getDeclaredMethod(targetClass, fallbackMethodName, newParameterTypes);
        if (fallbackMethod == null) {
            // 如果没有查找到，则查找不带异常的方法，注意这里要把参数变回来，即去掉 SdsException
            newArgs = args;
            fallbackMethod = this.getDeclaredMethod(targetClass, fallbackMethodName, parameterTypes);
        }
        if (fallbackMethod == null) {
            log.warn("找不到 {} 对应的降级方法，请检查您的注解配置", fallbackMethodName);
            return null;
        }
        try {
            return fallbackMethod.invoke(pjp.getThis(), newArgs);
        } catch (Exception e) {
            log.error("fallbackMethod 处理失败", e);
        }
        return null;
    }


    /**
     * 获取该类的声明的方法
     *
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    private Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, parameterTypes);

        } catch (NoSuchMethodException e) {
            log.warn("SdsPointAspect#getDeclaredMethod has exception", e);
        }

        return null;
    }

}
