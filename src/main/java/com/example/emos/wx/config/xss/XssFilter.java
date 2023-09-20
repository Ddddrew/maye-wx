package com.example.emos.wx.config.xss;


import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class XssFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;//数据类型强转
        XssHttpServletRequestWrapper wrapper=new XssHttpServletRequestWrapper(request);//调构造器
        filterChain.doFilter(wrapper,servletResponse);//调用方法，传的是转译后的数据
    }

    @Override
    public void destroy() {

    }
}

