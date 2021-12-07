package com.ma.config.shiro;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.omg.CORBA.PRIVATE_MEMBER;
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
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {

    @Autowired
    private ThreadLocalToken threadLocalToken;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate redisTemplate;



    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest req = (HttpServletRequest) request;
        String token = getRequestToken(req);

        if(StrUtil.isBlank(token)){
            return null;
        }

        return new OAuth2Token(token);
    }

    // 判断是否是OPTIONS请求， 是 放行， 否 Shiro过滤处理
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest req = (HttpServletRequest) request;
        if(req.getMethod().equals(RequestMethod.OPTIONS.name())){
            return true;
        }
        return false;
    }

   // 什么时候调用： 上面的那个方法返回 false，
    // 验证令牌 是否过期
    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",req.getHeader("Origin"));

        threadLocalToken.clear();

        String token = getRequestToken(req);
        if(StrUtil.isBlank(token)){
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return false;
        }
        try {
            jwtUtil.verifierToken(token);
        }catch (TokenExpiredException e){
            // 令牌过期

            //判断 redis缓存令牌
            if(redisTemplate.hasKey(token)){
                redisTemplate.delete(token);
                int userId = jwtUtil.getUserId(token);
                token = jwtUtil.createToken(userId);
                redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);

                threadLocalToken.setToken(token);
            }else {
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().print("令牌已过期");
                return false;
            }


        }catch (Exception e){
            // 无效令牌
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().print("无效的令牌");
            return  false;
        }

        // 通过这个方法 可以让 Shiro间接地执行 Realm类
        boolean bool = executeLogin(request,response);


        return bool;

    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setContentType("text/html");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",req.getHeader("Origin"));


        resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
        try {
            resp.getWriter().print(e.getMessage());
        } catch (Exception ex) {

        }

        return false;
    }


    @Override
    public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        super.doFilterInternal(request, response, chain);
    }

    private String getRequestToken(HttpServletRequest request){
        String token = request.getHeader("token");
        if(StrUtil.isBlank(token)){
            token = request.getParameter("token");
        }

        return token;
    }
}
