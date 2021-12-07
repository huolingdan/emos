package com.ma.controller.form;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Data
@ApiModel
public class LoginForm {

    @NotBlank(message = "微信临时授权不能为空")
    private String code;


}
