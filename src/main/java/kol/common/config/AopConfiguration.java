package kol.common.config;

import kol.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Aspect
@Configuration
@EnableScheduling
@Slf4j
public class AopConfiguration {

    /**
     * 非定时器切点
     */
    @Pointcut("!@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public void scheduledPoint() {
    }

    /**
     * 不记录切点的注解
     */
    @Pointcut("!@annotation(kol.common.annotation.NotLogRecord)")
    public void notLogRecordPoint() {
    }

    /**
     * service切点
     */
    @Pointcut("execution (* kol..service..*.*(..)) && scheduledPoint() && notLogRecordPoint())")
    public void servicePoint() {
    }

    /**
     * service 方法 around advice<br/>
     * 记录方法调用, 注意捕获的方法调用异常一定要抛出, 不然事务控制不能回滚事务
     *
     * @param proceedingJoinPoint
     * @return
     */
    @Around("servicePoint()")
    public Object printWebApiLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Object result = null;
        StringBuffer logBuffer = new StringBuffer();
        long startTime = System.currentTimeMillis();

        // try/catch保证日志记录处理出错时 不影响正常业务逻辑的执行
        try {
            MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
            Method targetMethod = methodSignature.getMethod();

            logBuffer.append("【Method】").append(targetMethod.getDeclaringClass().getName()).append(".")
                    .append(targetMethod.getName());
            logBuffer.append("\n");

            Object[] obj = proceedingJoinPoint.getArgs();
            if (ArrayUtils.isNotEmpty(obj)) {
                for (int i = 0, j = obj.length; i < j; i++) {
                    if (obj[i] instanceof HttpServletRequest || obj[i] instanceof HttpServletResponse) {
                        continue;
                    }
                    logBuffer.append(String.format("【arg%s】 %s: %s \n", i, obj[i] != null ?
                            obj[i].getClass().getName() : "param is null", JsonUtils.objectToJson(obj[i])));
                }
            }
        } catch (Exception e) {
            log.warn("log err:", e);
        }
        // 真正的业务逻辑方法调用
        try {
            result = proceedingJoinPoint.proceed();
        } catch (Throwable e) {
            log.info(logBuffer.toString());
            log.error("method invoke err:", e);
            // 方法执行的异常一定要抛出,不然事务控制不能回滚事务
            throw e;
        }

        logBuffer.append("【return】");
        // try/catch保证日志记录处理出错时 不影响正常业务逻辑的执行
        try {
            logBuffer.append(JsonUtils.objectToJson(result));
        } catch (Exception e) {
            log.warn("log err:", e);
        }
        logBuffer.append("\n").append("【run time】").append(System.currentTimeMillis() - startTime);
        log.info(logBuffer.toString());

        return result;
    }
}
