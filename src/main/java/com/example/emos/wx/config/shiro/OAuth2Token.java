package com.example.emos.wx.config.shiro;

import org.apache.shiro.authc.AuthenticationToken;

public class OAuth2Token implements AuthenticationToken {
    public String token;

    public OAuth2Token(String token) {
        this.token = token;
    }//构造器对封装类赋值

    @Override
    public Object getPrincipal() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }
}
