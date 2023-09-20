package com.example.emos.wx.controller;


import com.example.emos.wx.common.util.R;
import com.example.emos.wx.controller.form.TestSayHelloForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/test")
@Api("测试web接口")
public class TestController {

    @PostMapping("/sayHello")
    @ApiOperation("最简单的测试方法")
    public R sayHello(@Valid @RequestBody TestSayHelloForm form) {
        return R.ok().put("message", "Hello,"+form.getName());
    }//通过才会执行 封装成对象

    @PostMapping("/addUser")
    @ApiOperation("添加用户测试方法")
    //@RequiresPermissions(value = {"A","B"},logical = Logical.OR)
    @RequiresPermissions(value = {"root","USER:ADD"},logical = Logical.OR)
    public R addUser(){
        return R.ok("用户添加成功");
    }
}
