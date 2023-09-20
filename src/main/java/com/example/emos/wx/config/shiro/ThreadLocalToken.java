package com.example.emos.wx.config.shiro;

import org.springframework.stereotype.Component;

//媒介类
@Component
public class ThreadLocalToken {
    private ThreadLocal<String> local = new ThreadLocal<String>();

    public void setToken(String token) {
        local.set(token);
    }
    public String getToken(){
        return local.get();
    }
    public void clear(){
        local.remove();
    }
}
