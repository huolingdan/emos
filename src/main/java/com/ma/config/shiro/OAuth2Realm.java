package com.ma.config.shiro;

import com.ma.db.pojo.TbUser;
import com.ma.service.UserService;
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
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

//    授权(验证权限调用)
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        // 查询用户的权限列表
        // 把权限列表添加到info对象

        TbUser user = (TbUser) principals.getPrimaryPrincipal();
        int userId = user.getId();
        Set<String> permissions = userService.searchUserPermissions(userId);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permissions);

        return info;
    }

    //认证（登录时调用）
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        // 从令牌中获取userId,然后检测该用户是否被冻结


        String accessToken=(String)token.getPrincipal();
        int userId=jwtUtil.getUserId(accessToken);
        TbUser user=userService.searchById(userId);

        if(user==null){
            throw new LockedAccountException("账号已被锁定,请联系管理员");
        }
        SimpleAuthenticationInfo info=new SimpleAuthenticationInfo(user,accessToken,getName());


        // 往info对象中添加用户信息，Token字符串
        return info;
    }
}
