package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.pojo.TbUser;
import com.example.emos.wx.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;//用的工具类处理令牌字符串

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }//判断传入的令牌对象是不是封装的对象类型

    /*
    授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        TbUser user = (TbUser) principals.getPrimaryPrincipal();
        int userId= user.getId();
        Set<String> permsSet = userService.searchUserPermissions(userId);
        SimpleAuthorizationInfo info =new SimpleAuthorizationInfo();//授权对象
        info.setStringPermissions(permsSet);
        return info;
    }//授权方法

    /*
    认证(验证登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken=(String) token.getPrincipal();
        //从令牌中获得userId
        int userId= jwtUtil.getUserId(accessToken);
        TbUser user = userService.searchById(userId);

        if(user==null){
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }//离职了但是token还在

        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(user,accessToken,getName());
        //颁发认证对象，往info对象里面添加用户信息，token字符串
        return info;
    }//认证方法
}
