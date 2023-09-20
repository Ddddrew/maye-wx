package com.example.emos.wx.controller.form;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

//封装客户端提交的数据
@ApiModel
@Data
public class LoginForm {
    @NotBlank(message = "临时授权不能为空")
    private String code;

}
