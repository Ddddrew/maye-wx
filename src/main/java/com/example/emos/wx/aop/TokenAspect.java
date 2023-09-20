package com.example.emos.wx.aop;

import com.example.emos.wx.common.util.R;
import com.example.emos.wx.config.shiro.ThreadLocalToken;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TokenAspect {
    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Pointcut("execution(public * com.example.emos.wx.controller.*.*(..))")
    public void aspect(){

    }

    @Around("aspect()")//环绕事件
    public Object around(ProceedingJoinPoint point) throws Throwable{
        R r=(R)point.proceed();//方法执行结果
        String token=threadLocalToken.getToken();
        //如果ThreadLocalToken中存在Token，说明是更新的Token
        if(token!=null){
            r.put("token",token);//往响应里放Token
            threadLocalToken.clear();
        }
        return r;
    }
}
