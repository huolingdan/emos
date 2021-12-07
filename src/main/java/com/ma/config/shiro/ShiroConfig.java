package com.ma.config.shiro;


import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
public class ShiroConfig {

    @Bean("securityManager")
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm){

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(oAuth2Realm);
        securityManager.setRememberMeManager(null);
        return securityManager;
    }


    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,OAuth2Filter oAuth2Filter){

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        //oauth过滤

        Map<String, Filter> filters = new HashMap<>();

        filters.put("oauth2",oAuth2Filter);
        shiroFilterFactoryBean.setFilters(filters);

        Map<String,String> filterMap = new LinkedHashMap<>();

        filterMap.put("/webjars/**","anon");
        filterMap.put("/druid/**","anon");
        filterMap.put("/app/**","anon");
        filterMap.put("/sys/login","anon");
        filterMap.put("/v2/api-docs","anon");
        filterMap.put("/swagger-ui.html","anon");
        filterMap.put("/swagger-resources/**","anon");
        filterMap.put("/user/register","anon");
        filterMap.put("/user/login","anon");
        filterMap.put("/test/**","anon");
        filterMap.put("/**","oauth2");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);

        return shiroFilterFactoryBean;
    }


    @Bean("lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){

        return new LifecycleBeanPostProcessor();
    }


    @Bean("authorizationAttributeSourceAdvisor")
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager){


        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);

        return advisor;
    }

}
