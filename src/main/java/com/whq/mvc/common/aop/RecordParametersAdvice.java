package com.whq.mvc.common.aop;

import com.alibaba.fastjson.JSON;
import com.whq.mvc.common.annotation.RecordParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description: 记录日志切面
 * @Author: whq
 * @Date: 2021/8/31 11:06
 */
@Slf4j
@Aspect
@Order
@Configuration
@RequiredArgsConstructor
public class RecordParametersAdvice {

    //栈帧局部变量表参数名侦察器
    private static final LocalVariableTableParameterNameDiscoverer PARAMETER_NAME_DISCOVERER =
            new LocalVariableTableParameterNameDiscoverer();

    //void类型名称
    private static final String VOID_SRT = void.class.getName();

    //controller类型后缀
    private static final String CONTROLLER_SRT = "Controller";

    private final AopSupport aopSupport;


    @Pointcut(
            "("
                    + "@within(com.whq.mvc.common.annotation.RecordParameters)"
                    + "||"
                    + "@annotation(com.whq.mvc.common.annotation.RecordParameters)"
                    + "||"
                    + "execution(* com.whq.mvc.controller..*.*(..))"
                    + ")"
                    + "&&"
                    + "!@annotation(com.whq.mvc.common.annotation.IgnoreRecordParameters)"
    )
    public void executeAdvice() {

    }

    /**
     * @Description: 环绕增强
     * @Author: whq
     * @Date: 2021/8/31 16:16
     */
    @Around("executeAdvice()")
    public Object aroundAdvice(ProceedingJoinPoint thisJoinPoint) throws Throwable {
        Object targetObj = thisJoinPoint.getTarget();
        Class<?> targetClazz = targetObj.getClass();
        String clazzName = targetClazz.getName();
        MethodSignature methodSignature = (MethodSignature) thisJoinPoint.getSignature();
        Method targetMethod = methodSignature.getMethod();
        //方法上的注解RecordParameters
        RecordParameters annotation = targetMethod.getAnnotation(RecordParameters.class);
        if (null == annotation) {
            //类上的注解RecordParameters
            annotation = targetClazz.getAnnotation(RecordParameters.class);
            //通过execution触发的
            if (null == annotation && clazzName.endsWith(CONTROLLER_SRT)) {
                annotation = (RecordParameters) AnnotationUtils.getDefaultValue(RecordParameters.class);
            }
        }

        //定义一些变量判断
        boolean shouldRecordInputParam;
        boolean shouldRecordOutputParam;
        RecordParameters.LogLevel logLevel;
        boolean isControllerMethod;

        if (null != annotation) {
            shouldRecordInputParam = (annotation.strategy() == RecordParameters.Strategy.INPUT) ||
                    (annotation.strategy() == RecordParameters.Strategy.INPUT_OUTPUT);
            shouldRecordOutputParam = (annotation.strategy() == RecordParameters.Strategy.OUTPUT) ||
                    (annotation.strategy() == RecordParameters.Strategy.INPUT_OUTPUT);
            logLevel = annotation.loglevel();
            isControllerMethod = clazzName.endsWith(CONTROLLER_SRT);
        } else {
            shouldRecordInputParam = shouldRecordOutputParam = true;
            logLevel = RecordParameters.LogLevel.INFO;
            isControllerMethod = true;
        }
        final String classMethodInfo = "Class#Method -> " + clazzName + "#" + targetMethod.getName();
        if (shouldRecordInputParam) {
            preHandle(thisJoinPoint, logLevel, targetMethod, classMethodInfo, isControllerMethod);
        }
        Object obj = thisJoinPoint.proceed();
        if (shouldRecordOutputParam) {
            postHandle(logLevel,targetMethod,obj,classMethodInfo,isControllerMethod);
        }
        return obj;
    }

    //输入参数前置处理
    private void preHandle(ProceedingJoinPoint pjp, RecordParameters.LogLevel logLevel, Method targetMethod,
                           String classMethodInfo, boolean isControllerMethod) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("\n【the way in】");
        if (isControllerMethod) {
            sb.append("request-path[").append(aopSupport.getRequestPath()).append("] ");
        }
        sb.append(classMethodInfo);
        Object[] parameterValues = pjp.getArgs();
        if (null != parameterValues || parameterValues.length > 0) {
            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(targetMethod);
            if (null == parameterNames) {
                throw new RuntimeException("parameterNames must not be null!");
            }
            sb.append(", with parameters ↓↓");
            for (int i = 0; i < parameterValues.length; i++) {
                sb.append("\n\t").append(parameterNames[i]).append(" ==>").append(aopSupport.jsonPretty(parameterValues[i]));
                if (i == parameterNames.length - 1) {
                    sb.append("\n");
                }
            }
        } else {
            sb.append(", without and parameters");
        }
        aopSupport.log(logLevel, sb.toString());
    }

    //输出参数后置处理
    private void postHandle(RecordParameters.LogLevel logLevel, Method targetMethod, Object obj,
                            String classMethodInfo, boolean isControllerMethod) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("\n【the way out】");
        if (isControllerMethod) {
            sb.append("request-path[").append(aopSupport.getRequestPath()).append("] ");
        }
        sb.append(classMethodInfo);
        Class<?> returnClass = targetMethod.getReturnType();
        sb.append("\n\treturn type → ").append(returnClass);
        if (!VOID_SRT.equals(returnClass.getName())) {
            sb.append("\n\treturn result → ").append(aopSupport.jsonPretty(obj));
        }
        sb.append("\n");
        aopSupport.log(logLevel, sb.toString());
    }

    @Component
    static class AopSupport {
        private static Class<?> logClass = log.getClass();
        private static Map<String, Method> methodMap = new ConcurrentHashMap<>(8);

        /**
         * @Description: 初始化日志级别对应的日志方法
         * @Author: whq
         * @Date: 2021/8/31 16:56
         */
        @PostConstruct
        private void init() throws NoSuchMethodException {
            String debugStr = RecordParameters.LogLevel.DEBUG.name();
            String infoStr = RecordParameters.LogLevel.INFO.name();
            String warnStr = RecordParameters.LogLevel.WARN.name();
            Method debugMethod = log.getClass().getMethod(debugStr.toLowerCase(), String.class, Object.class);
            Method infoMethod = log.getClass().getMethod(infoStr.toLowerCase(), String.class, Object.class);
            Method warnMethod = log.getClass().getMethod(warnStr.toLowerCase(), String.class, Object.class);
            methodMap.put(debugStr, debugMethod);
            methodMap.put(infoStr, infoMethod);
            methodMap.put(warnStr, warnMethod);
        }

        /**
         * @Description: 记录日志
         * @Author: whq
         * @Date: 2021/8/31 16:57
         */
        private void log(RecordParameters.LogLevel logLevel, Object markerValue) {
            try {
                methodMap.get(logLevel.name()).invoke(log, "{}", markerValue);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("RecordParametersAdvice$AopSupport#log occur error!", e);
            }
        }

        /**
         * @Description: json格式化输出
         * @Author: whq
         * @Date: 2021/8/31 17:01
         */
        private String jsonPretty(Object obj) {
            return JSON.toJSONString(obj);
        }

        /**
         * @Description: 获取请求路径
         * @Author: whq
         * @Date: 2021/8/31 17:02
         */
        private String getRequestPath() {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (null == requestAttributes) {
                log.warn("obtain request-path is empty");
                return "";
            }
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return request.getRequestURI();
        }
    }
}
