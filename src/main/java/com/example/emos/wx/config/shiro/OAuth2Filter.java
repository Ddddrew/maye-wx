package com.example.emos.wx.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")//多例对象，否则用多少次都只创建一个对象
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocalToken;//媒介类

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;//值注入

    @Autowired
    private JwtUtil jwtUtil;//调用这里面的方法进行令牌的校验

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req= (HttpServletRequest) request;
        String token=getRequestToken(req);//调用下面私有的方法，接受从请求头或者请求体里的令牌
        if(StrUtil.isBlank(token)){
            return null;
        }
        return new OAuth2Token(token);//把令牌字符串封装成令牌对象
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req= (HttpServletRequest) request;
        if(req.getMethod().equals(RequestMethod.OPTIONS.name())){
            //判断是不是options类型的，如果是就直接放行
            return true;
        }
        return false;
    }//判断请求是否需要被shiro框架处理

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse resp= (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));//跨域设置

        threadLocalToken.clear();//清空数据

        String token=getRequestToken(req);
        if(StrUtil.isBlank(token)){
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);//设置状态码
            resp.getWriter().print("无效的令牌");
            return false;//不执行认证
        }//令牌字符串判断是否为空
        try{
            jwtUtil.verifierToken(token);//验证令牌
        }catch (TokenExpiredException e){//令牌过期
            if(redisTemplate.hasKey(token)){//服务端redis缓存令牌没有过期，生成新的令牌
                redisTemplate.delete(token);//把老令牌删掉
                int userId=jwtUtil.getUserId(token);//从老令牌里获得userId
                token=jwtUtil.createToken(userId);//创建新令牌，传入参数userId
                redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);//保存令牌
                threadLocalToken.setToken(token);//媒介类里也存令牌
            }
            else{//客户端、服务端都过期了
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }
        }catch (Exception e){//不正确的令牌
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        boolean bool=executeLogin(request,response);//没有问题，让shiro执行
        return bool;
    }


    @Override
    //认证失败时触发这个方法
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse resp= (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try{
            resp.getWriter().print(e.getMessage());//认证失败消息
        }catch (Exception exception){

        }

        return false;
    }

    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest req= (HttpServletRequest) request;
        HttpServletResponse resp= (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        super.doFilterInternal(request, response, chain);
    }

    private String getRequestToken(HttpServletRequest request){
        String token=request.getHeader("token");
        if(StrUtil.isBlank(token)){
            token=request.getParameter("token");//从请求体里获得数据
        }
        return token;
    }
}
