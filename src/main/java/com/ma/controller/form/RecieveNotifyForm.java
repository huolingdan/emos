package com.ma.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel
public class RecieveNotifyForm {
    @NotBlank
    private String processId;

    private String uuid;

    private String result;
}
