package com.ma.config.shiro;

import org.springframework.stereotype.Component;


// 存储令牌的媒介类
@Component
public class ThreadLocalToken {

    private ThreadLocal<String> local = new ThreadLocal();

    public void setToken(String token){
        local.set(token);
    }

    public String getToken(){
        return (String) local.get();
    }

    public void clear(){
        local.remove();
    }
}
