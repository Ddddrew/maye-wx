package com.example.emos.wx.config.shiro;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
@Slf4j //日志
public class JwtUtil {
    @Value("${emos.jwt.secret}") //把配置类里的值注入
    public String secret;

    @Value("${emos.jwt.expire}")
    private int expire;

    public String createToken(int userId){
        DateTime date = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, 5);//偏移五天
        Algorithm algorithm=Algorithm.HMAC256(secret);//加密算法对象
        JWTCreator.Builder builder=JWT.create();//内部类对象
        String token=builder.withClaim("userId",userId).withExpiresAt(date).sign(algorithm);//绑定属性
        return token;
    }

    public int getUserId(String token){
        DecodedJWT jwt=JWT.decode(token);//创建解码对象
        int userId=jwt.getClaim("userId").asInt();//从令牌字符串里反向得到userId
        return userId;
    }

    public void verifierToken(String token){
        Algorithm algorithm=Algorithm.HMAC256(secret);//创建算法对象
        JWTVerifier verifier=JWT.require(algorithm).build();//创建验证对象
        verifier.verify(token);//调用验证方法
    }
}
