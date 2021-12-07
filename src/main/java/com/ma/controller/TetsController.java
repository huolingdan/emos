package com.ma.controller;


import com.ma.common.util.R;
import com.ma.controller.form.TestForm;
import com.ma.service.CheckinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Api("测试接口")
@RequestMapping("test")
public class TetsController {

    @Autowired
    private CheckinService checkinService;

    @PostMapping("/sayhello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestForm form){
        return R.ok().put("msg","hello"+form.getName());
    }

    @PostMapping("/addUser")
    @ApiOperation("添加用户")
    @RequiresPermissions(value = {"A","B"},logical = Logical.OR)
    public R addUser(){


        return R.ok("用户添加成功");
    }


}
