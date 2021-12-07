package com.ma.controller;


import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.ma.common.util.R;
import com.ma.config.shiro.JwtUtil;
import com.ma.config.tencent.TLSSigAPIv2;
import com.ma.controller.form.*;
import com.ma.db.dao.TbUserDao;
import com.ma.db.pojo.MessageEntity;
import com.ma.exception.EmosException;
import com.ma.service.UserService;
import com.ma.task.MessageTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@Api("用户模块Web接口")
@RequestMapping("user")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageTask messageTask;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Value("${trtc.appid}")
    private Integer appid;

    @Value("${trtc.key}")
    private String key;

    @Value("${trtc.expire}")
    private Integer expire;



    @ApiOperation("注册用户")
    @PostMapping("/register")
    public R register(@Valid @RequestBody RegisterForm form){

        int id = userService.registerUser(form.getRegisterCode(),form.getCode(),form.getNickname(), form.getPhoto());
        String token = jwtUtil.createToken(id);
        Set<String> permsSet = userService.searchUserPermissions(id);
        //把令牌缓存到redis
        saveCacheToken(token,id);


        return R.ok("用户注册成功").put("token",token).put("permission",permsSet);
    }

    @PostMapping("/login")
    @ApiOperation("登录系统")
    public R login(@Valid @RequestBody LoginForm form){

        int id = userService.login(form.getCode());
        String token = jwtUtil.createToken(id);
        saveCacheToken(token,id);
        Set<String> perms = userService.searchUserPermissions(id);

        return R.ok("登录成功").put("token",token).put("perms",perms);
    }

    @GetMapping("/searchUserSummary")
    @ApiOperation("查询用户摘要信息")
    public R searchUserSummary(@RequestHeader("token") String token){

        int userId = jwtUtil.getUserId(token);

        HashMap map = userService.searchUserSummary(userId);
        return R.ok().put("result",map);

    }

    @PostMapping("/searchUserGroupByDept")
    @ApiOperation("查询员工列表，按照部门分组排列")
    @RequiresPermissions(value = {"ROOT","EMPLOYEE:SELECT"},logical = Logical.OR)
    public R searchUserGroupByDept(@Valid @RequestBody SearchUserGroupByDeptForm form){


        ArrayList<HashMap> list = userService.searchUserGroupByDept(form.getKeyword());

        return R.ok().put("result",list);

    }

    @PostMapping("/searchMembers")
    @ApiOperation("查询成员")
    @RequiresPermissions(value = {"ROOT","MEETING:INSERT","MEETING:UPDATE"},logical = Logical.OR)
    public R searchMembers(@Valid @RequestBody SearchMembersForm form){
        if (!JSONUtil.isJsonArray(form.getMembers())){
            throw new EmosException("members不是JSON数组");
        }
        List param = JSONUtil.parseArray(form.getMembers()).toList(Integer.class);
        ArrayList list = userService.searchMembers(param);
        return R.ok().put("result",list);
    }

    @PostMapping("/selectUserPhotoAndName")
    @ApiOperation("查询用户姓名和头像")
    public R selectUserPhotoAndName(@Valid @RequestBody SelectUserPhotoAndNameForm form){
        if(!JSONUtil.isJsonArray(form.getIds())){
            throw new EmosException("参数不是JSON数组");
        }
        List<Integer> param = JSONUtil.parseArray(form.getIds()).toList(Integer.class);
        List<HashMap> list = userService.selectUserPhotoAndName(param);
        return R.ok().put("result",list);
    }


    @GetMapping("/genUserSig")
    @ApiOperation("生成用户签名")
    public R genUserSig(@RequestHeader("token") String token){
        int id=jwtUtil.getUserId(token);
        String email=userService.searchMemberEmail(id);
        TLSSigAPIv2 api=new TLSSigAPIv2(appid,key);
        String userSig=api.genUserSig(email,expire);
        return R.ok().put("userSig",userSig).put("email",email);
    }


    private void saveCacheToken(String token,int userId){
        redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
    }


}
